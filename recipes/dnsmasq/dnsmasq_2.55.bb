DESCRIPTION = "dnsmasq"
PR = "r0"
SRC_URI = "file://${PN}-${PV}.tar.gz \
	   file://dhcp.conf     \
	   file://dnsmasq.conf  \
	   file://dnsmasq.init	\
           file://101-ipv6.patch"

S = "${WORKDIR}/${PN}-${PV}"

inherit autotools

EXTRA_OECONF = "--disable-nls"

do_install() {
	install -d ${D}/etc/config -d ${D}/etc/init.d -d ${D}${sbindir} 

	install -m0644 ${WORKDIR}/dnsmasq.conf ${D}/etc
	install -m0644 ${WORKDIR}/dhcp.conf ${D}/etc/config/dhcp
	install -m0755 ${WORKDIR}/dnsmasq.init ${D}/etc/init.d/dnsmasq
	install -m0755 src/dnsmasq ${D}${sbindir}
}
