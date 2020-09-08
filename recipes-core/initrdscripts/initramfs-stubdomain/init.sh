#!/bin/sh
#
# Copyright (c) 2013 Citrix Systems, Inc.
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
#

export PATH="/sbin:$PATH"

mount -t devtmpfs none /dev

exec 0<&-
exec 1<&-
exec 2<&-

exec 0< /dev/hvc0
exec 1> /dev/hvc0
exec 2> /dev/hvc0

sync
mkdir -p /proc /sys /mnt /tmp
mount -t proc proc /proc
mount -t xenfs none /proc/xen
mount -t sysfs sysfs /sys

# Cmdline parsing.
KERNEL_CMDLINE=`cat /proc/cmdline`
for arg in $KERNEL_CMDLINE; do
    case "$arg" in
        debug) LOGLVL="debug";;
        gdb) GDB="on";;
        *) continue ;;
    esac
done

modprobe xen-argo
modprobe ivc

echo "Xen chardevs:"
echo /dev/xen/*

if [ "${LOGLVL}" = "debug" ]; then
    cut -f1,2,3,4,5 -d ' ' /proc/modules
fi

test -c "/dev/argo_dgram" || echo "/dev/argo_dgram is missing."
test -c "/dev/argo_stream" || echo "/dev/argo_stream is missing."
test -c "/dev/ivc" || echo "/dev/ivc is missing."

echo "0" > /sys/bus/pci/drivers_autoprobe
for pci_dev in `ls /sys/bus/pci/devices/`
do
 if [ -e /sys/bus/pci/devices/$pci_dev/driver/unbind ]
 then
    echo "pci device $pci_dev is bound, unbind it!"
    echo "$pci_dev" > /sys/bus/pci/devices/$pci_dev/driver/unbind
 fi
done

ln -s /proc/self/fd/2 /dev/stderr

echo 1 > /proc/sys/net/ipv4/ip_forward
echo 0 > /proc/sys/net/ipv4/conf/all/rp_filter

mkdir -p /var/run
export USE_INTEL_SB=1
export INTEL_DBUS=1

echo "Starting qemu directly."
target="$( xenstore-read target )"
vm_uuid="$( xenstore-read /local/domain/${target}/vm )"
dmargs="$( xenstore-read ${vm_uuid}/image/dmargs )"
echo "target $target vm_uuid $vm_uuid"
echo "Invoking qemu with dmargs       = ${dmargs}"

if [ "${GDB}" = "on" ]; then
  cat - >> /tmp/cmds <<EOF
  add-auto-load-safe-path /lib/libthread_db-1.0.so
  run
  backtrace
EOF
  gdb -x /tmp/cmds --args /usr/bin/qemu-system-i386 ${dmargs}
else
  /usr/bin/qemu-system-i386 ${dmargs}
  echo "qemu-system-i386 terminated with code $?."
fi

poweroff -f
