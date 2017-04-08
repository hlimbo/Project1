#python 3 script
#Developed in python version 3.5.3
import os
import sys

class ConvertException (Exception):
    pass

class WriteException (Exception):
    pass

#For now, schema is tuned by hand inside this file

#movie to game db translation
#Conversion part
# movies -> games
# stars -> publishers
# stars_in_movies -> publishers_of_games
# genres -> genres
# genres_in_movies -> genres_of_games
#Random generation part
# customers -> customers
# sales -> sales
# creditcards -> creditcards

#schema = {tableName : [columnName]}
schema = {"Games" : ["id","Rank","Name","Year","NA_Sales","EU_Sales","JP_Sales","Other_Sales","Global_Sales"], 
        "Genres" : ["id","Genre"],
        "GenresOfGames" : ["Games","Genres"],
        "Publishers" : ["id","Publisher"],
        "PublishersOfGames" : ["Games","Publishers"],
        "Platforms" : ["id","Platform"],
        "PlatformsOfGames" : ["Games","Platforms"]}
dupCheck=['Genre','Publisher',"Platform"]
additionalInfo=[('Games','id')]
uniques = {}
for table in dupCheck:
    uniques[table] = {}
counts = {}
for table in schema:
    counts[table]=1
def findTable (column):
    for table, columns in schema.items():
        if column in columns:
            return table

#order is the order that things show up in the csv file
order= list(map (lambda column : (findTable(column),column), 
        ["Rank", "Name","Platform","Year","Genre","Publisher",
            "NA_Sales","EU_Sales","JP_Sales","Other_Sales","Global_Sales"]))

#references are tables that are related to each other
references = {}
for foreignKey in schema:
    for table, columns in schema.items():
        if foreignKey in columns:
            if table not in references:
                references[table]=[]
            references[table].append(foreignKey)
for table in references:
    for i in range(0,len(schema[table])):
        schema[table][i]=references[table][i][:-1]+"_id"
#types = {columnName : columnType}
#most values are string type, so set all to that
types = {}
for value in order:
    types[value[1]]="str"
types["Rank"]="int"
types["Year"]="year"
types["id"]="int"
for table in references:
    for i in range(0,len(schema[table])):
        types[references[table][i][:-1]+"_id"]="int"

def insertHeader (inserts,insertCounts,tableName,insertID=True):
    inserts[tableName]="INSERT INTO "+tableName+"\n   ("
    #put all but last value of table schema into insert statement
    for column in schema[tableName][:-1]:
        inserts[tableName]+=column+", "
    #insert last column without a comma after it
    inserts[tableName]+=schema[tableName][-1]+") VALUES \n   ("
    if insertID:
        inserts[tableName]+=str(counts[tableName])+", "
        insertCounts[tableName]=1
    else:
        insertCounts[tableName]=0

def convert (csvFileName, newSqlFileName, skipFirstLine=False):
    if not os.path.exists(csvFileName):
        raise ConvertException("CSV file "+csvFileName+" could not be found!")
    if os.path.exists(newSqlFileName):
        raise ConvertException("SQL file "+newSqlFileName+" already exists!")
    i=0
    #keep track of inserted values in order to deal with references
    with open (csvFileName,'r') as csv, open(newSqlFileName,'w') as sql:
        exists = {}
        if skipFirstLine:
            csv.readline()
        for line in csv:
            fields = {}
            #write insert statement by table instead of csv order
            inserts = {}
            insertCounts = {}
            #skip lines with null data
            if line.find("N/A") != -1:
                continue
            line=line.replace("'","\\'")
            #replace non-ASCII characters
            line=line.encode("ascii",'replace').decode("ascii")
            quote = line.find("\"")
            #remove commas within quotes
            while quote!= -1:
                end = quote+1+line[quote+1:].find("\"")
                #assumes every quotation is ended in the same line
                assert (end!=-1)
                line=line.replace(line[quote:end+1],line[quote:end+1].replace(",","").replace("\"",""),1)
                quote=line.find("\"")
            assert (line.find("\"")==-1)
            for value in line.split(","):
                #replace semicolons with colons so file can be post processed
                value=value.replace(";",",")
                checkForDuplicates=False
                fieldType = types[order[i][1]]
                if order[i][1] in dupCheck:
                    checkForDuplicates=True
                if fieldType == "str":
                    if checkForDuplicates and (order[i][0],"'"+value.strip()+"'") in uniques:
                        fields[order[i]]="'"+value.strip()+"'"
                        i+=1
                        if i%len(order) == 0:
                            i=0
                        continue
                elif fieldType == "int":
                    if checkForDuplicates and (order[i][0],value.strip()) in uniques:
                        fields[order[i]]=value.strip()
                        i+=1
                        if i%len(order) == 0:
                            i=0
                        continue
                #insert top part of INSERT statement
                if order[i][0] not in inserts:
                    insertHeader(inserts,insertCounts,order[i][0])
                    fields[(order[i][0],schema[order[i][0]][0])]=counts[order[i][0]]
                if fieldType == "str":
                    inserts[order[i][0]]+="'"+value.strip()+"'"
                    fields[order[i]]="'"+value.strip()+"'"
                elif fieldType == "int":
                    inserts[order[i][0]]+=value.strip()
                    fields[order[i]]=value.strip()
                elif fieldType == "year":
                    inserts[order[i][0]]+=value.strip()
                    fields[order[i]]=value.strip()
                else:
                    raise ConvertException("Unknown type "+fieldType)
                if checkForDuplicates:
                    uniques[(order[i][0],fields[order[i]])]=fields[(order[i][0],schema[order[i][0]][0])]
                insertCounts[order[i][0]]+=1
                assert (insertCounts[order[i][0]] <= len(schema[order[i][0]])), "insertions longer than schema!"
                #table insert statement complete, write out to file
                if insertCounts[order[i][0]] == len(schema[order[i][0]]):
                    inserts[order[i][0]]+=");\n"
                    sql.write(inserts[order[i][0]])
                    del inserts[order[i][0]]
                    insertCounts[order[i][0]]=0
                    counts[order[i][0]]+=1
                else:
                    inserts[order[i][0]]+=", "
                i+=1
                if i%len(order) == 0:
                    i=0
            #handle relationships now that entities are assumed to be processed
            for child,parents in references.items():
                for parent in parents:
                    for column in schema[parent]:
                        if (parent,column) in additionalInfo:
                            if child not in inserts:
                                insertHeader(inserts,insertCounts,child,False)
                            inserts[child]+=str(fields[(parent,column)])
                            insertCounts[child]+=1
                            if insertCounts[child] == len(schema[child]):
                                inserts[child]+=");\n"
                                sql.write(inserts[child])
                                del inserts[child]
                                insertCounts[child]=0
                                counts[child]+=1
                            else:
                                inserts[child]+=", "
                    for field in fields.values():
                        if (parent,field) in uniques:
                            if child not in inserts:
                                insertHeader(inserts,insertCounts,child,False)
                            inserts[child]+=str(uniques[(parent,field)])
                            insertCounts[child]+=1
                            if insertCounts[child] == len(schema[child]):
                                inserts[child]+=");\n"
                                sql.write(inserts[child])
                                del inserts[child]
                                insertCounts[child]=0
                                counts[child]+=1
                            else:
                                inserts[child]+=", "
    if i!=0:
        raise ConvertException(csvFileName+" could not be properly parsed! Columns not properly defined!")
    for table, count in insertCounts.items():
        if count != 0:
            raise ConvertException(csvFileName+" could not be properly parsed! Table "+table
                    +" doesn't match with count "+str(count))
    return

def writeCreateType (sqlFile,colType):
    if colType =="str":
        sqlFile.write("VARCHAR(200)")
    elif colType == "int":
        sqlFile.write("INTEGER")
    elif colType == "year":
        sqlFile.write("YEAR")
    else:
        raise WriteException("Unknown type "+colType)

def writeColumn (sqlFile,col):
    sql.write(col+" ")
    writeCreateType(sql,types[col])

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Which csv file you wish to convert?")
        sys.argv.append(input())
    if len(sys.argv) < 3:
        print("Which sql file you wish to output to?")
        sys.argv.append(input())
    try:
        if len(sys.argv) >= 4 and sys.argv[3].lower()=="true":
            convert(sys.argv[1],sys.argv[2], True)
        else:
            convert(sys.argv[1],sys.argv[2])
        #post process sql file to combine insertions for a table
        #into one insertion statement to make running the sql file faster
        with open(sys.argv[2],"r") as sql, open("_new_"+sys.argv[2],"w") as newSql:
            statement = ""
            values={}
            columns={}
            for table in schema:
                values[table]=[]
                columns[table]=[]
            for line in sql:
                statement+=line
                if line.find(";") != -1:
                    statementPrefix="INSERT INTO "
                    assert(statement.find(statementPrefix) != -1)
                    start=statement.find(statementPrefix)+len(statementPrefix)
                    table = statement[start:statement[start:].find(" ")+start].strip()
                    columnStart = statement.find(table)+len(table)
                    statementValues = "VALUES"
                    assert(statement.find(statementValues) != -1)
                    columns[table] = statement[columnStart:statement.find(statementValues)].strip()
                    valueStart = statement.find(statementValues)+len(statementValues)
                    values[table].append(statement[valueStart:statement.find(";")].strip())
                    statement=""
            for table in schema:
                newSql.write("INSERT INTO "+table+
                        "\n   "+columns[table]+" VALUES\n   ")
                for value in values[table][:-1]:
                    newSql.write(value+",\n   ")
                newSql.write(values[table][-1]+";\n\n")
        #replace file with optimized version
        os.rename("_new_"+sys.argv[2],sys.argv[2])
        #write createtable script 
        with open("create_"+sys.argv[2],"w") as sql:
            #for entity tables
            for table in schema:
                if table not in references:
                    sql.write("CREATE TABLE "+table+" (\n   ")
                    #create key field
                    writeColumn(sql,schema[table][0])
                    sql.write(" PRIMARY KEY NOT NULL AUTO_INCREMENT,\n   ")
                    for column in schema[table][1:-1]:
                        writeColumn(sql,column)
                        sql.write(",\n   ")
                    writeColumn(sql,schema[table][-1])
                    sql.write("\n);\n\n")
            #for relationship tables
            for table in schema:
                if table in references:
                    sql.write("CREATE TABLE "+table+" (\n   ")
                    #write values first
                    writeColumn(sql,schema[table][0])
                    sql.write(" NOT NULL")
                    for column in schema[table][1:]:
                        sql.write(",\n   ");
                        writeColumn(sql,column)
                        sql.write(" NOT NULL")
                    #now write foreign key constraints
                    #for column in schema[table]:
                    #    parent = column[:-3]+"s"
                    #    sql.write(",\n   CONSTRAINT "+"fk_"+table+"_"+parent+" FOREIGN KEY (`"+column+"`) REFERENCES `"+parent+"`(id) ON DELETE CASCADE")
                    #    #sql.write(",\n   FOREIGN KEY (`"+column+"`) REFERENCES `"+parent)
                    sql.write("\n);\n\n")
                    #sql.write("\n) ENGINE=InnoDB DEFAULT CHARSET=latin1;\n\n")
    except ConvertException as e:
        print(e)
