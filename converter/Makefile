all:
	rm -f vgsales.sql
	rm -f create_vgsales.sql
	python3 convert.py vgsales.csv vgsales.sql true
	mysql < del.sql
	mysql gamedb  < create_vgsales.sql
	mysql gamedb < vgsales.sql
