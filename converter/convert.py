#python 3 script
#Developed in python version 3.5.3
import os
import sys
import string
import random
#import statements after this point are dependencies
#forgery_py under an MIT license, used to generate 
# names, addresses, emails, and dates
import forgery_py

random.seed()

def generateEmail ():
    return forgery_py.internet.email_address(forgery_py.internet.domain_name())

def generateExpDate ():
    #up to 10 years
    return forgery_py.date.date(max_delta=3560)

def generateAddress ():
    forge = forgery_py.address
    return (forge.street_address()+", "+forge.city()+", "+
            forge.state_abbrev()+" "+forge.zip_code())

def generatePassword ():
    password=""
    allowed = string.ascii_letters+string.digits
    #allowed = (string.ascii_letters+string.digits+
    #    string.punctuation.replace("-","").replace("'","").replace("\"","")).replace("\\","")
    for i in range(0,20):
        password+=allowed[random.randint(0,len(allowed)-1)]
    return password

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
# ? -> platforms  #fewer platforms than there are publishers. 30 versus 500
# ? -> platforms_of_games
#Random generation part
# customers -> customers
# sales -> sales
# creditcards -> creditcards

#schema = {tableName : [columnName]}
schema = {"games" : ["id","rank","name","year","globalsales"], 
        "genres" : ["id","genre"],
        "genres_of_games" : ["games","genres"],
        "publishers" : ["id","publisher"],
        "publishers_of_games" : ["games","publishers"],
        "platforms" : ["id","platform"],
        "platforms_of_games" : ["games","platforms"],
        "creditcards" : ["id", "first_name", "last_name", "expiration"],
        "customers" : ["id", "cc_id", "first_name", "last_name",
            "address", "email", "password"],
        "sales" : ["id","customer_id","salesdate","games"]}
additionalDependency={("customers","cc_id") : "creditcards"}
dupCheck=['genre','publisher',"platform"]
ignore=['NA_Sales','EU_Sales','JP_Sales','Other_Sales']
#additionalInfo is field values that need to be tracked for dependencies
additionalInfo=[('games','id')]
def findTable (column):
    for table, columns in schema.items():
        if column in columns:
            return table

#order is the order that things show up in the csv file
order= list(map (lambda column : (findTable(column),column), 
        ["rank", "name","platform","year","genre","publisher",
            "NA_Sales","EU_Sales","JP_Sales","Other_Sales","globalsales"]))

#references are tables that are related to each other
references = {}
for foreignKey in schema:
    for table, columns in schema.items():
        if foreignKey in columns:
            if table not in references:
                references[table]=[]
            references[table].append(foreignKey)
            index=schema[table].index(foreignKey)
            schema[table][index]=schema[table][index][:-1]+"_id"
#additional dependency information
dependencyOrder = ["creditcards","customers","sales"]
#types = {columnName : columnType}
#most values are string type, so set all to that
types = {}
for value in order:
    types[value[1]]="str"
types["cc_id"]="int"
types["first_name"]="str"
types["last_name"]="str"
types["address"]="str"
types["email"]="str"
types["password"]="str"
types["customer_id"]="int"
types["rank"]="int"
types["year"]="year"
types["expiration"]="date"
types["salesdate"]="date"
types["id"]="int"
for table in references:
    for i in range(0,len(schema[table])):
        if schema[table][i].endswith("_id"):
            types[schema[table][i]]="int"

def generateCreditCard():
    return {"first_name" : "'"+forgery_py.name.first_name()+"'", 
            "last_name" : "'"+forgery_py.name.last_name()+"'",
            "expiration" : "'"+str(generateExpDate())+"'"}

def generateCustomer(card):
    return {"cc_id" : str(card), "first_name" : "'"+forgery_py.name.first_name()+"'", 
            "last_name" : "'"+forgery_py.name.last_name()+"'", 
            "address" : "'"+generateAddress()+"'", "email" : "'"+generateEmail()+"'",
            "password" : "'"+generatePassword()+"'"}

def generateSale(customer,game):
    return {"customer_id" : str(customer), "game_id" : str(game), 
            "salesdate" : "'"+str(forgery_py.date.date(past=True, max_delta=3560))+"'"}

def insertDictRecord (sql,inserts,insertCounts,counts,table,record):
    insertHeader(inserts,insertCounts,counts,table)
    for field in schema[table][1:]:
        inserts[table]+=record[field]
        postInsert(sql,inserts,insertCounts,counts,table)
    return counts[table]-1

#tables with randomly generated data are paired with the function 
#that generates them
def generateInitialSale (sql,inserts,insertCounts,counts,fields):
    game = str(fields[('games','id')])
    card = str(insertDictRecord(sql,inserts,insertCounts,counts,"creditcards",generateCreditCard()))
    cust = str(insertDictRecord(sql,inserts,insertCounts,counts,"customers",generateCustomer(card)))
    insertDictRecord(sql,inserts,insertCounts,counts,"sales",generateSale(cust,game))

preloads = {"creditcards" : generateInitialSale,"customers" : generateInitialSale,"sales" : generateInitialSale }

def insertHeader (inserts,insertCounts,counts,tableName,insertID=True):
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

def postInsert (sql,inserts,insertCounts,counts,tableName):
    insertCounts[tableName]+=1
    assert (insertCounts[tableName] <= len(schema[tableName])), "insertions longer than schema!"
    #table insert statement complete, write out to file
    if insertCounts[tableName] == len(schema[tableName]):
        inserts[tableName]+=");\n"
        sql.write(inserts[tableName])
        del inserts[tableName]
        insertCounts[tableName]=0
        counts[tableName]+=1
    else:
        inserts[tableName]+=", "

def convert (csvFileName, newSqlFileName, skipFirstLine=False):
    if not os.path.exists(csvFileName):
        raise ConvertException("CSV file "+csvFileName+" could not be found!")
    if os.path.exists(newSqlFileName):
        raise ConvertException("SQL file "+newSqlFileName+" already exists!")
    i=0
    #keep track of inserted values in order to deal with references
    with open (csvFileName,'r') as csv, open(newSqlFileName,'w') as sql:
        if skipFirstLine:
            csv.readline()
        uniques = {}
        counts = {}
        for table in schema:
            counts[table]=1
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
                if fieldType.startswith("str"):
                    if (checkForDuplicates and (order[i][0],"'"+value.strip()+"'") in uniques) or order[i][1] in ignore:
                        fields[order[i]]="'"+value.strip()+"'"
                        i+=1
                        if i%len(order) == 0:
                            i=0
                        continue
                elif fieldType == "int":
                    if (checkForDuplicates and (order[i][0],value.strip()) in uniques) or order[i][1] in ignore:
                        fields[order[i]]=value.strip()
                        i+=1
                        if i%len(order) == 0:
                            i=0
                        continue
                #insert top part of INSERT statement
                if order[i][0] not in inserts:
                    insertHeader(inserts,insertCounts,counts,order[i][0])
                    fields[(order[i][0],schema[order[i][0]][0])]=counts[order[i][0]]
                if fieldType.startswith("str"):
                    inserts[order[i][0]]+="'"+value.strip()+"'"
                    fields[order[i]]="'"+value.strip()+"'"
                elif fieldType == "int":
                    inserts[order[i][0]]+=value.strip()
                    fields[order[i]]=value.strip()
                elif fieldType == "year":
                    inserts[order[i][0]]+=value.strip()
                    fields[order[i]]=value.strip()
                elif fieldType == "date":
                    inserts[order[i][0]]+=value.strip()
                    fields[order[i]]=value.strip()
                else:
                    raise ConvertException("Unknown type "+fieldType)
                if checkForDuplicates:
                    uniques[(order[i][0],fields[order[i]])]=fields[(order[i][0],schema[order[i][0]][0])]
                postInsert(sql,inserts,insertCounts,counts,order[i][0])
                i+=1
                if i%len(order) == 0:
                    i=0
            #handle relationships now that entities are assumed to be processed
            for child,parents in references.items():
                if child in preloads:
                    preloads[child](sql,inserts,insertCounts,counts,fields)
                    continue
                for column in schema[child]:
                    if len(column) > 3:
                        parent = column[:-3]+'s'
                    else:
                        parent = None
                    if parent in schema:
                        for parentColumn in schema[parent]:
                            if (parent,parentColumn) in additionalInfo:
                                if child not in inserts:
                                    insertHeader(inserts,insertCounts,counts,child,False)
                                inserts[child]+=str(fields[(parent,parentColumn)])
                                postInsert(sql,inserts,insertCounts,counts,child)
                                continue
                        for key, field in fields.items():
                            if (parent,field) in uniques:
                                if child not in inserts:
                                    insertHeader(inserts,insertCounts,counts,child,False)
                                inserts[child]+=str(uniques[(parent,field)])
                                postInsert(sql,inserts,insertCounts,counts,child)
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
    elif colType.startswith("str"):
        sqlFile.write("VARCHAR("+colType[3:]+")")
    elif colType == "int":
        sqlFile.write("INTEGER")
    elif colType == "year":
        sqlFile.write("YEAR")
    elif colType == "date":
        sqlFile.write("DATE")
    else:
        raise WriteException("Unknown type "+colType)

def writeColumn (sqlFile,col):
    sql.write(col+" ")
    writeCreateType(sql,types[col])

def writeInsert (sqlFile,columns,values,tableName):
    newSql.write("INSERT INTO "+tableName+
            "\n   "+columns[tableName]+" VALUES\n   ")
    for value in values[tableName][:-1]:
        newSql.write(value+",\n   ")
    newSql.write(values[tableName][-1]+";\n\n")

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
            #handle entities first
            for table in schema:
                if table not in references:
                    writeInsert(newSql,columns,values,table)
            #handle relationships now
            for table in schema:
                if table in references:
                    writeInsert(newSql,columns,values,table)
        #replace file with optimized version
        os.rename("_new_"+sys.argv[2],sys.argv[2])
        #write createtable script 
        with open("create_"+sys.argv[2],"w") as sql:
            #for entity tables
            for table in schema:
                if table not in references and table not in dependencyOrder:
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
                if table in references and table not in dependencyOrder:
                    sql.write("CREATE TABLE "+table+" (\n   ")
                    #write values first
                    writeColumn(sql,schema[table][0])
                    sql.write(" NOT NULL")
                    for column in schema[table][1:]:
                        sql.write(",\n   ");
                        writeColumn(sql,column)
                        sql.write(" NOT NULL")
                    #now write foreign key constraints
                    for column in schema[table]:
                        if len(column) < 3 or not column.endswith("_id"):
                            continue
                        parent = column[:-3]+"s"
                        if parent in schema:
                            sql.write(",\n   CONSTRAINT "+"fk_"+table+"_"+parent+" FOREIGN KEY (`"+column+"`) REFERENCES `"+parent+"`(id) ON DELETE CASCADE")
                    sql.write("\n);\n\n")
            #for tables with a dependencyOrder
            for table in dependencyOrder:
                sql.write("CREATE TABLE "+table+" (\n   ")
                #write values first
                writeColumn(sql,schema[table][0])
                sql.write(" PRIMARY KEY NOT NULL AUTO_INCREMENT")
                #now write foreign key constraints
                for column in schema[table][1:]:
                    sql.write(",\n   ");
                    writeColumn(sql,column)
                    sql.write(" NOT NULL")
                for column in schema[table][1:]:
                    if (table,column) in additionalDependency:
                        parent = additionalDependency[(table,column)]
                    else:
                        if len(column) < 3 or not column.endswith("_id"):
                            continue
                        parent = column[:-3]+"s"
                    if parent in schema:
                        sql.write(",\n   CONSTRAINT "+"fk_"+table+"_"+parent+" FOREIGN KEY (`"+column+"`) REFERENCES `"+parent+"`(id) ON DELETE CASCADE")
                sql.write("\n);\n\n")
    except ConvertException as e:
        print(e)
