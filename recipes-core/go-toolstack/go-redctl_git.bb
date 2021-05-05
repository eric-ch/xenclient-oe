SUMMARY = "Redfield Toolstack (redctl)"
HOMEPAGE = "http://www.gitlab.com/redfield/redctl"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/src/${GO_IMPORT}/LICENSE;md5=38f7323dc81034e5b69acc12001d6eb1"

DEPENDS = " \
    xen-tools \
    grpc \
"

inherit go pkgconfig systemd update-rc.d

GO_IMPORT = "gitlab.com/redfield/redctl"
SRC_URI = " \
    git://gitlab.com/redfield/redctl.git;branch=openxt-4.12;protocol=https;destsuffix=${BPN}-${PV}/src/${GO_IMPORT} \
    file://redctld.service \
    file://redctld-shutdown.service \
    file://tmpfiles-redctld.conf \
    file://redctld.initscript \
"

SRCREV = "${AUTOREV}"

GO_LINKSHARED = ""
GOBUILDMODE = 'exe'

RDEPENDS_${PN} = "bash bash-completion"
RDEPENDS_${PN}-dev = "bash make"

do_install_append() {
    DESTDIR=${D} make -C src/${GO_IMPORT} install

    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -d ${D}${systemd_system_unitdir}
        install -m 0644 ${WORKDIR}/redctld.service ${D}${systemd_system_unitdir}
        install -m 0644 ${WORKDIR}/redctld-shutdown.service ${D}${systemd_system_unitdir}

        install -d -m 755 ${D}${sysconfdir}/tmpfiles.d
        install -p -m 644 ${WORKDIR}/tmpfiles-redctld.conf ${D}${sysconfdir}/tmpfiles.d/
    else
        install -d ${D}${sysconfdir}/init.d
        install -m 0755 ${WORKDIR}/redctld.initscript ${D}${sysconfdir}/init.d/redctld
    fi
}

SYSTEMD_SERVICE_${PN} = "redctld.service redctld-shutdown.service"

INITSCRIPT_NAME = "redctld"
INITSCRIPT_PARAMS = "defaults 80"
INITSCRIPT_PACKAGES = "redctld"
