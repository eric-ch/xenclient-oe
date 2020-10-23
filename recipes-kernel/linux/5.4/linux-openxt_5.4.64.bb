DESCRIPTION = "Linux kernel for OpenXT service VMs."

# Use the one from meta-openembedded/meta-oe
require recipes-kernel/linux/linux.inc
require recipes-kernel/linux/linux-openxt.inc
DEPENDS += "rsync-native"

PV_MAJOR = "${@"${PV}".split('.', 3)[0]}"

FILESEXTRAPATHS_prepend := "${THISDIR}/patches:${THISDIR}/defconfigs:"
SRC_URI += "${KERNELORG_MIRROR}/linux/kernel/v${PV_MAJOR}.x/linux-${PV}.tar.xz;name=kernel \
    file://0001-nvme-pci-disable-the-write-zeros-command-for-Intel-6.patch \
    file://0001-xen-gntdev-switch-from-kcalloc-to-kvcalloc.patch \
    file://bridge-carrier-follow-prio0.patch \
    file://xenkbd-tablet-resolution.patch \
    file://acpi-video-delay-init.patch \
    file://skb-forward-copy-bridge-param.patch \
    file://dont-suspend-xen-serial-port.patch \
    file://extra-mt-input-devices.patch \
    file://tpm-log-didvid.patch \
    file://blktap2.patch \
    file://intel-amt-support.patch \
    file://disable-csum-xennet.patch \
    file://pci-pt-move-unaligned-resources.patch \
    file://pci-pt-flr.patch \
    file://realmem-mmap.patch \
    file://netback-skip-frontend-wait-during-shutdown.patch \
    file://xenbus-move-otherend-watches-on-relocate.patch \
    file://netfront-support-backend-relocate.patch \
    file://konrad-ioperm.patch \
    file://usbback-base.patch \
    file://hvc-kgdb-fix.patch \
    file://pciback-restrictive-attr.patch \
    file://thorough-reset-interface-to-pciback-s-sysfs.patch \
    file://tpm-tis-force-ioremap.patch \
    file://netback-vwif-support.patch \
    file://xen-txt-add-xen-txt-eventlog-module.patch \
    file://xenpv-no-tty0-as-default-console.patch \
    file://xsa-155-qsb-023-add-RING_COPY_RESPONSE.patch \
    file://xsa-155-qsb-023-xen-blkfront-make-local-copy-of-response-before-usin.patch \
    file://xsa-155-qsb-023-xen-blkfront-prepare-request-locally-only-then-put-i.patch \
    file://xsa-155-qsb-023-xen-netfront-add-range-check-for-Tx-response-id.patch \
    file://xsa-155-qsb-023-xen-netfront-copy-response-out-of-shared-buffer-befo.patch \
    file://xsa-155-qsb-023-xen-netfront-do-not-use-data-already-exposed-to-back.patch \
    file://tpm_tis-work-around-status-register-bug-in-STMicroelectronics-TPM.patch \
    file://defconfig \
    file://xenbus-param-wait-nonessentials.patch \
    "

LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"
SRC_URI[kernel.md5sum] = "28a29677c102b211359439f78d1f3d6a"
SRC_URI[kernel.sha256sum] = "b9d3c2938466f388a70fd190fd6691baa8b757393b267e9f7b06c4730d85d5ef"

do_configure_append_openxt-installer(){
    # vglass supports radeon and nouveau DRM.
    kernel_conf_variable DRM_RADEON m
    kernel_conf_variable DRM_NOUVEAU m

    # vglass can handle touchscreen and pen devices.
    kernel_conf_variable I2C_HID m
    kernel_conf_variable HID_WACOM m
    ## Designware
    kernel_conf_variable I2C_DESIGNWARE m
    kernel_conf_variable I2C_DESIGNWARE_PLATFORM m
    ## HP Elite x2
    kernel_conf_variable PINCTRL_SUNRISEPOINT m
    kernel_conf_variable MFD_INTEL_LPSS_PCI m
}

do_configure_append_xenclient-dom0(){
    # vglass supports radeon and nouveau DRM.
    kernel_conf_variable DRM_RADEON m
    kernel_conf_variable DRM_NOUVEAU m

    # vglass can handle touchscreen and pen devices.
    kernel_conf_variable I2C_HID m
    kernel_conf_variable HID_WACOM m
    ## Designware
    kernel_conf_variable I2C_DESIGNWARE m
    kernel_conf_variable I2C_DESIGNWARE_PLATFORM m
    ## HP Elite x2
    kernel_conf_variable PINCTRL_SUNRISEPOINT m
    kernel_conf_variable MFD_INTEL_LPSS_PCI m
}

do_configure_append_xenclient-uivm(){
    # openxtfb requires framebuffer support. Use XEN_FBDEV_FRONTEND to select
    # all required support.
    kernel_conf_variable FB_MODE_HELPERS y
    kernel_conf_variable XEN_FBDEV_FRONTEND m
}
