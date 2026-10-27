# Permit passwordless root login only for development builds targeting QEMU.
IMAGE_FEATURES:append:qemuall = " allow-empty-password empty-root-password allow-root-login"
