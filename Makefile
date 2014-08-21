clean:
	rm -rfv cfg
	rm -rfv Makefile
	rm -rfv README.md
	rm -rfv src
all: clean
	mv WebContent/* .
	rm -rfv WebContent