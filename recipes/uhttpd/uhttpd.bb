DESCRIPTION = "uhttpd"
PR = "r0"
SRC_URI = "file://${PN}.tar.gz \
           file://uhttpd.init \
           file://uhttpd.config"

DEPENDS += "openssl"

S = "${WORKDIR}/${PN}"

INITSCRIPT_NAME = "uhttpd"

inherit autotools pkgconfig binconfig

do_compile() {
	oe_runmake LUA_SUPPORT="" TLS_SUPPORT="" UHTTPD_TLS="" TLS_CFLAGS="" TLS_LDFLAGS=""
}

do_install() {
	install -d -m0755 ${D}/etc/init.d
	install -m 0755 ${WORKDIR}/uhttpd.init ${D}/etc/init.d/uhttpd
	install -d -m0755 ${D}/etc/config
	install -m0600 ${WORKDIR}/uhttpd.config ${D}/etc/config/uhttpd
	install -d -m0755 ${D}/usr/sbin
	install -m0755 uhttpd ${D}/usr/sbin
}

