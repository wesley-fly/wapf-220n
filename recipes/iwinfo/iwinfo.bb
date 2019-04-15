DESCRIPTION = "openwrt iwinfo"
PR = "r0"
SRC_URI = "file://${PN}.tar.gz \
	   file://wireless"

DEPENDS += "lua"

S = "${WORKDIR}/${PN}"

CFLAGS_append += "-D_GNU_SOURCE"

EXTRA_OEMAKE = "'CC=${CC}' 'FPCI=-fPIC'" 

do_install() {
	install -d -m0755 ${D}/usr/lib
	install -m0755 libiwinfo.so ${D}/usr/lib

	install -d -m0755 ${D}/usr/lib/lua
	install -m0755 iwinfo.so ${D}/usr/lib/lua

	install -d -m0755 ${D}/etc/config
	install -m0644 ${WORKDIR}/wireless  ${D}/etc/config
}

FILES_${PN} += "/usr/lib/lua/iwinfo.so"
FILES_${PN} += "/usr/lib/libiwinfo.so"
FILES_${PN}-dbg += "/usr/lib/lua/.debug/iwinfo.so"

