JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Client.java 
	# Client.java \
	# ClientAlt.java \
	# ListenAlt.java

TEST = \
    Test.java

default: classes

test: $(TEST:.java=.class)

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

