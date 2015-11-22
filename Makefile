# Makefile for lab 7, part 2

CC  = gcc
CXX = g++

INCLUDES = 
CFLAGS   = -g -Wall $(INCLUDES)
CXXFLAGS = -g -Wall $(INCLUDES)

LDFLAGS =
LDLIBS =

http-server:

.PHONY: clean
clean:
	rm -f *.o *~ a.out core http-server

.PHONY: val
val: http-server
	valgrind --leak-check=full --show-reachable=yes -v --error-exitcode=1 ./http-server 8888 /home/bab2209/html beijing 9997

.PHONY: all
all: clean default

