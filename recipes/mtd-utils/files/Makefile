%.c%.o: %.c
	$(CC) $(CFLAGS) -o %.o -c %.c

all: flashcp

flashcp: flashcp.o
	$(CC) $(LDFLAGS) -o $@ $@.o

install: all
	install -d ${DESTDIR}/usr/bin
	install -m 0755 flashcp ${DESTDIR}/usr/bin
clean:
	rm -rf flashcp *.o

