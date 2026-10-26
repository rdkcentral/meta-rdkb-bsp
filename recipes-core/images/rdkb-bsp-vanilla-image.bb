SUMMARY = "Minimal vanilla image for RDK-B BSP integration"
LICENSE = "MIT"

IMAGE_INSTALL = "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"

inherit core-image
