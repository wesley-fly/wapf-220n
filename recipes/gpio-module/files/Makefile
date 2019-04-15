PWD = $(shell pwd)
KBUILD_FLAGS = -C $(KERNEL_PATH) M=$(PWD) KERNELRELEASE=$(KERNELRELEASE)

obj-m += gpio.o

all:default

default:
	$(MAKE) $(KBUILD_FLAGS) modules

clean:
	rm -rf *.o *.ko .*.cmd *mod.c .tmp_versions

