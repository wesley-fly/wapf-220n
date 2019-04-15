DESCRIPTION = "flashcp"
SECTION = "base"
PRIORITY = "optional"
LICENSE = "GPL"


INITSCRIPT_NAME = "flashcp"

SRC_URI = "\
	file://flashcp.c \
	file://Makefile \
"

S = "${WORKDIR}"

do_compile() {
	oe_runmake
}

do_install() {
	oe_runmake DESTDIR=${D} install
}
