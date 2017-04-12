#python 3 script
#Developed in python version 3.5.3
import os
import codecs
import sys
import string
import random
#import statements after this point are dependencies
#forgery_py under an MIT license, used to generate 
# names, addresses, emails, and dates
import forgery_py
#unidecode under GPL license
from unidecode import unidecode


random.seed()

def generateEmail ():
    return forgery_py.internet.email_address(forgery_py.name.company_name())

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
        #a publisher of a game publishes it for a particular platform
        "publishers_of_games" : ["games","publishers","platforms"],
        "platforms" : ["id","platform"],
        "platforms_of_games" : ["games","platforms"],
        "creditcards" : ["id", "first_name", "last_name", "expiration"],
        "customers" : ["id", "cc_id", "first_name", "last_name",
            "address", "email", "password"],
        "sales" : ["id","customer_id","salesdate","games"]}
additionalDependency={("customers","cc_id") : "creditcards"}
dupCheck=[('genres','genre'),('publishers','publisher'),('platforms','platform'),('game','name')]
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
types[("creditcards","id")]="str"
for table in references:
    for i in range(0,len(schema[table])):
        if schema[table][i].endswith("_id"):
            types[schema[table][i]]="int"
types["cc_id"]="str"


def generateCreditCardNumber ():
    num = "" 
    for i in range(0,16):
        num+=str(0)
    while num in generateCreditCardNumber._generated:
        num=""
        for i in range(0,16):
            num+=str(random.randint(0,9))
    generateCreditCardNumber._generated[num]=True
    return num
generateCreditCardNumber._generated={"0000000000000000" : True}

def generateCreditCard():
    return {"id" : "'"+generateCreditCardNumber()+"'",
            "first_name" : "'"+forgery_py.name.first_name()+"'", 
            "last_name" : "'"+forgery_py.name.last_name()+"'",
            "expiration" : "'"+str(generateExpDate())+"'"}

def generateCustomer(card):
    return {"cc_id" : str(card), 
            "first_name" : "'"+forgery_py.name.first_name()+"'", 
            "last_name" : "'"+forgery_py.name.last_name()+"'", 
            "address" : "'"+generateAddress()+"'",
            "email" : "'"+generateEmail()+"'",
            "password" : "'"+generatePassword()+"'"}

def generateSale(customer,game):
    return {"customer_id" : str(customer),
            "game_id" : str(game), 
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
    card = generateCreditCard()
    insertHeader(inserts,insertCounts,counts,"creditcards",False)
    for field in schema["creditcards"]:
        inserts["creditcards"]+=card[field]
        postInsert(sql,inserts,insertCounts,counts,"creditcards")
    cust = str(insertDictRecord(sql,inserts,insertCounts,counts,"customers",generateCustomer(card["id"])))
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

def clearInsert (inserts,insertCounts,tableName):
    del inserts[tableName]
    insertCounts[tableName]=0

def postInsert (sql,inserts,insertCounts,counts,tableName):
    insertCounts[tableName]+=1
    assert (insertCounts[tableName] <= len(schema[tableName])), "insertions longer than schema!"
    #table insert statement complete, write out to file
    if insertCounts[tableName] == len(schema[tableName]):
        inserts[tableName]+=");\n"
        sql.write(inserts[tableName])
        clearInsert(inserts,insertCounts,tableName)
        counts[tableName]+=1
    else:
        inserts[tableName]+=", "


def getFieldValue (fieldType,value):
    if fieldType.startswith("str"):
        return "'"+value.strip()+"'"
    elif fieldType == "int":
        return value.strip()
    elif fieldType == "year":
        return value.strip()
    elif fieldType == "date":
        return value.strip()
    else:
        raise ConvertException("Unknown type "+fieldType)

def convert (csvFileName, newSqlFileName, skipFirstLine=False):
    #reset card numbers available
    generateCreditCardNumber._generated={"0000000000000000" : True}
    if not os.path.exists(csvFileName):
        raise ConvertException("CSV file "+csvFileName+" could not be found!")
    if os.path.exists(newSqlFileName):
        raise ConvertException("SQL file "+newSqlFileName+" already exists!")
    i=0
    #TODO insert postLine code where applicable
    def postLine ():
        i+=1
        if i%len(order) == 0:
            i=0
    #keep track of inserted values in order to deal with references
    with open (csvFileName,'r') as csv, open(newSqlFileName,'w') as sql:
        if skipFirstLine:
            csv.readline()
        uniques = {}
        counts = {}
        for table in schema:
            counts[table]=1
        skipLine = {}
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
            #line=line.encode("utf_8",'replace').decode("ascii")
            line=unidecode(line).encode("ascii",'replace').decode("ascii")
            quote = line.find("\"")
            #remove commas within quotes
            while quote!= -1:
                end = quote+1+line[quote+1:].find("\"")
                #assumes every quotation is ended in the same line
                assert (end!=-1)
                line=line.replace(line[quote:end+1],line[quote:end+1].replace(",","").replace("\"",""),1)
                quote=line.find("\"")
            assert (line.find("\"")==-1)
            for table in schema:
                skipLine[table]=False
            for value in line.split(","):
                table = order[i][0]
                column = order[i][1]
                if column in ignore:
                    i+=1
                    if i%len(order) == 0:
                        i=0
                    continue
                #replace semicolons with colons so file can be post processed
                value=value.replace(";",",")
                if (table,column) in types:
                    fieldType = types[(table,column)]
                else:
                    fieldType = types[column]
                fields[order[i]]=getFieldValue(fieldType,value)
                #store id
                if table not in inserts:
                    fields[(table,schema[table][0])]=counts[table]
                if (table,column) in dupCheck:
                    if (order[i],fields[order[i]]) in uniques:
                        fields[(table,schema[table][0])]=uniques[(order[i],fields[order[i]])]
                        skipLine[table]=True
                    else:
                        uniques[(order[i],fields[order[i]])]=fields[(table,schema[table][0])]
                if skipLine[table]:
                    #remove values currently stored for this row
                    if table in inserts:
                        clearInsert(inserts,insertCounts,table)
                    i+=1
                    if i%len(order) == 0:
                        i=0
                    continue
                #insert top part of INSERT statement
                if table not in inserts:
                    insertHeader(inserts,insertCounts,counts,table)
                inserts[table]+=getFieldValue(fieldType,value)
                postInsert(sql,inserts,insertCounts,counts,table)
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
                            elif (parent,parentColumn) in dupCheck:
                                if child not in inserts:
                                    insertHeader(inserts,insertCounts,counts,child,False)
                                inserts[child]+=str(uniques[((parent,parentColumn),
                                    fields[(parent,parentColumn)])])
                                postInsert(sql,inserts,insertCounts,counts,child)
    if i!=0:
        raise ConvertException(csvFileName+" could not be properly parsed! Columns not properly defined!")
    for table, count in insertCounts.items():
        if count != 0:
            raise ConvertException(csvFileName+" could not be properly parsed! Table "+table
                    +" doesn't match with count "+str(count)+"\nDataLeft:\n"+inserts[table]+"\n")
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

def writeColumn (sqlFile,tableName,col):
    sql.write(col+" ")
    if (tableName,col) in types:
        writeCreateType(sql,types[(tableName,col)])
    else:
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
                if table not in references and table not in dependencyOrder:
                    writeInsert(newSql,columns,values,table)
            #handle relationships now
            for table in schema:
                if table in references and table not in dependencyOrder:
                    writeInsert(newSql,columns,values,table)
            #handle dependencyOrder last
            for table in dependencyOrder:
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
                    writeColumn(sql,table,schema[table][0])
                    if (table,schema[table][0]) in types:
                        colType = types[(table,schema[table][0])]
                    else:
                        colType = types[schema[table][0]]
                    if colType == "int":
                        sql.write(" PRIMARY KEY NOT NULL AUTO_INCREMENT,\n   ")
                    else:
                        sql.write(" PRIMARY KEY NOT NULL,\n   ")
                    for column in schema[table][1:-1]:
                        writeColumn(sql,table,column)
                        sql.write(",\n   ")
                    writeColumn(sql,table,schema[table][-1])
                    sql.write("\n);\n\n")
            #for relationship tables
            for table in schema:
                if table in references and table not in dependencyOrder:
                    sql.write("CREATE TABLE "+table+" (\n   ")
                    #write values first
                    writeColumn(sql,table,schema[table][0])
                    sql.write(" NOT NULL")
                    for column in schema[table][1:]:
                        sql.write(",\n   ");
                        writeColumn(sql,table,column)
                        sql.write(" NOT NULL")
                    #now write foreign key constraints
                    for column in schema[table]:
                        if len(column) < 3 or not column.endswith("_id"):
                            continue
                        parent = column[:-3]+"s"
                        if parent in schema:
                            sql.write(",\n   CONSTRAINT "+"fk_"+table+"_"+parent+" FOREIGN KEY (`"+
                                    column+"`) REFERENCES `"+parent+"`("+schema[parent][0]+") ON DELETE CASCADE")
                    sql.write("\n);\n\n")
            #for tables with a dependencyOrder
            for table in dependencyOrder:
                sql.write("CREATE TABLE "+table+" (\n   ")
                #write values first
                writeColumn(sql,table,schema[table][0])
                if (table,schema[table][0]) in types:
                    colType = types[(table,schema[table][0])]
                else:
                    colType = types[schema[table][0]]
                if colType == "int":
                    sql.write(" PRIMARY KEY NOT NULL AUTO_INCREMENT")
                else:
                    sql.write(" PRIMARY KEY NOT NULL")
                #now write foreign key constraints
                for column in schema[table][1:]:
                    sql.write(",\n   ");
                    writeColumn(sql,table,column)
                    sql.write(" NOT NULL")
                for column in schema[table][1:]:
                    if (table,column) in additionalDependency:
                        parent = additionalDependency[(table,column)]
                    else:
                        if len(column) < 3 or not column.endswith("_id"):
                            continue
                        parent = column[:-3]+"s"
                    if parent in schema:
                        sql.write(",\n   CONSTRAINT "+"fk_"+table+"_"+parent+" FOREIGN KEY (`"+
                                column+"`) REFERENCES `"+parent+"`("+schema[parent][0]+") ON DELETE CASCADE")
                sql.write("\n);\n\n")
    except ConvertException as e:
        print(e)
