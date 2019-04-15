DESCRIPTION = "uci"
PR = "r0"
SRC_URI = "file://${PN}-${PV}.tar.gz \
           file://120-uci_trigger.patch \
           file://130-lua_fix_nested_foreach_delete.patch \
	   file://140-uci_fix_reorder.patch \
           file://150-lua_expose_add_list_changes.patch \
	   file://uci.sh"

DEPENDS += "readline lua"

S = "${WORKDIR}/uci-${PV}"

INITSCRIPT_NAME = "uci"

inherit autotools pkgconfig binconfig

EXTRA_OEMAKE = "'CC=${CC}'"

do_compile() {
	oe_runmake LIBS="${LDFLAGS} -lc -ldl"
	oe_runmake LIBS="${LDFLAGS} -shared -Wl,-soname, -L.. -luci" -C lua
}

do_install() {
	install -d -m0755 ${D}/sbin
        install -m0755 uci  ${D}/sbin

	install -d -m0755 ${D}/usr/lib/lua
	install -m0755 lua/uci.so ${D}/usr/lib/lua

	install -d -m0755 ${D}/lib
	install -m0755 libuci.so.0.8 ${D}/lib
	cp -a  libuci.so ${D}/lib
	
	install -d -m0755 ${D}/lib/config
	install -m0755 ${WORKDIR}/uci.sh ${D}/lib/config/uci.sh
}


FILES_${PN} += "/lib/config/*"
FILES_${PN} += "/lib/libuci.so"
FILES_${PN} += "/usr/lib/lua"
FILES_S{PN} += "/usr/lib/lua/uci.so"


