SECTION = "console/network"
DESCRIPTION = "Point-to-Point Protocol (PPP) daemon"
HOMEPAGE = "http://samba.org/ppp/"
DEPENDS = "libpcap"
LICENSE = "BSD GPLv2"
PR = "r9"

SRC_URI = "file://ppp-${PV}.tar.gz\
	   file://010-use_target_for_configure.patch \
	   file://100-debian_ip-ip_option.patch \
	   file://101-debian_close_dev_ppp.patch \
	   file://102-debian_fix_close_fd0.patch \
	   file://103-debian_fix_link_pidfile.patch \
	   file://104-debian_fix_mschapv2_ppp.patch \
           file://105-debian_demand.patch \
	   file://106-debian_stripMSdomain.patch \
           file://107-debian_pppatm_cleanup.patch \
	   file://108-debian_pppatm_fix_mtu.patch \
	   file://109-debian_pppoe_cleanup.patch \
           file://110-debian_defaultroute.patch \
	   file://200-makefile.patch \
	   file://201-mppe_mppc_1.1.patch \
           file://202-no_strip.patch \
           file://203-opt_flags.patch \
           file://204-radius_config.patch \
           file://205-no_exponential_timeout.patch \
	   file://206-compensate_time_change.patch \
	   file://207-lcp_mtu_max.patch \
           file://300-filter-pcap-includes-lib.patch \
	   file://310-precompile_filter.patch \
	   file://320-custom_iface_names.patch \
	   file://330-retain_foreign_default_routes.patch \
	   file://340-populate_default_gateway.patch \
           file://350-survive_bad_pads_packets.patch \
	   file://500_fix_make.patch \
	   file://ip-up \
	   file://ip-down \
	   file://ipv6-up \
           file://ipv6-down \
           file://options \
           file://chap-secrets \
           file://filter \
           file://ppp.sh \
           file://pppoe.sh"

inherit autotools

EXTRA_OEMAKE = "STRIPPROG=${STRIP} MANDIR=${D}${datadir}/man/man8 INCDIR=${D}/usr/include LIBDIR=${D}/usr/lib/pppd/${PV} BINDIR=${D}/usr/sbin"
EXTRA_OECONF = "--disable-strip"

do_install_append () {
        make INCDIR=${D}/${includedir} install-devel
        make install-etcppp ETCDIR=${D}/${sysconfdir}/ppp

	mkdir -p ${D}${sysconfdir}/ppp
	
	install -m 0755 ${WORKDIR}/ip-up 	${D}${sysconfdir}/ppp/
	install -m 0755 ${WORKDIR}/ip-down 	${D}${sysconfdir}/ppp/
	install -m 0755 ${WORKDIR}/ipv6-up 	${D}${sysconfdir}/ppp/
	install -m 0755 ${WORKDIR}/ipv6-down 	${D}${sysconfdir}/ppp/
	install -m 0755 ${WORKDIR}/options 	${D}${sysconfdir}/ppp/
	install -m 0755 ${WORKDIR}/chap-secrets	${D}${sysconfdir}/ppp/
	install -m 0755 ${WORKDIR}/filter	${D}${sysconfdir}/ppp/

	mkdir -p ${D}/lib/network
	install -m 0755 ${WORKDIR}/ppp.sh ${D}/lib/network	
	install -m 0755 ${WORKDIR}/pppoe.sh ${D}/lib/network	

	rm -rf ${D}/${mandir}/man8/man8
}

CONFFILES_${PN} = "${sysconfdir}/ppp/pap-secrets ${sysconfdir}/ppp/chap-secrets ${sysconfdir}/ppp/options"
PACKAGES += "ppp-oa ppp-oe ppp-radius ppp-winbind ppp-minconn ppp-password ppp-tools"
FILES_${PN}        = "/etc /usr/bin /usr/sbin/chat /usr/sbin/pppd /lib/network/ppp.sh"
FILES_${PN}_nylon  = "/etc /usr/bin /usr/sbin/chat /usr/sbin/pppd /usr/sbin/tdbread"
FILES_${PN}-dbg += "${libdir}/pppd/2.4.4/.debug"
FILES_ppp-oa       = "/usr/lib/pppd/2.4.4/pppoatm.so"
FILES_ppp-oe       = "/usr/sbin/pppoe-discovery /usr/lib/pppd/2.4.4/rp-pppoe.so /lib/network/pppoe.sh"
FILES_ppp-radius   = "/usr/lib/pppd/2.4.4/radius.so /usr/lib/pppd/2.4.4/radattr.so /usr/lib/pppd/2.4.4/radrealms.so"
FILES_ppp-winbind  = "/usr/lib/pppd/2.4.4/winbind.so"
FILES_ppp-minconn  = "/usr/lib/pppd/2.4.4/minconn.so"
FILES_ppp-password = "/usr/lib/pppd/2.4.4/pass*.so"
FILES_ppp-tools    = "/usr/sbin/pppstats /usr/sbin/pppdump"
DESCRIPTION_ppp-oa       = "Plugin for PPP needed for PPP-over-ATM"
DESCRIPTION_ppp-oe       = "Plugin for PPP needed for PPP-over-Ethernet"
DESCRIPTION_ppp-radius   = "Plugin for PPP that are related to RADIUS"
DESCRIPTION_ppp-winbind  = "Plugin for PPP to authenticate against Samba or Windows"
DESCRIPTION_ppp-minconn  = "Plugin for PPP to specify a minimum connect time before the idle timeout applies"
DESCRIPTION_ppp-password = "Plugin for PPP to get passwords via a pipe"
DESCRIPTION_ppp-tools    = "The pppdump and pppstats utitilities"
RDEPENDS_ppp_minconn    += "libpcap0.8"

pkg_postinst_${PN}() {
if test "x$D" != "x"; then
	exit 1
else
	chmod u+s ${sbindir}/pppd
fi
}

SRC_URI[md5sum] = "848f6c3cafeb6074ffeb293c3af79b7c"
SRC_URI[sha256sum] = "1e0fddb5f53613dd14ab10b25435e88092fed1eff09b4ac4448d5be01f3b0b11"
