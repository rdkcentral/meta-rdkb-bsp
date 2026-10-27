SUMMARY = "Minimal vanilla image for RDK-B BSP integration"
LICENSE = "MIT"

IMAGE_INSTALL = "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"

# Permit passwordless root login only for development builds targeting QEMU.
IMAGE_FEATURES:append:qemuall = " allow-empty-password empty-root-password allow-root-login"

inherit core-image
