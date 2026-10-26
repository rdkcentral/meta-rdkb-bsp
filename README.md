# meta-rdkb-bsp

`meta-rdkb-bsp` is a Yocto Project / OpenEmbedded layer containing BSP metadata for ARM-based platforms targeting RDK-B broadband devices.

The layer provides platform-specific configuration and integration required to build RDK-B images for supported ARM hardware.

## Scope

This layer may contain:

- Machine configurations
- BSP-specific recipes
- Kernel configuration and patches
- Bootloader configuration
- Device tree integration
- Firmware and hardware-specific dependencies
- Platform-specific overrides and configuration

The goal is to keep hardware and BSP-specific metadata separated from the RDK-B runtime and application layers.

## Supported Platforms

The layer is intended to support multiple ARM SoCs and reference platforms used for broadband gateways and networking devices.

Platform support will be added under the appropriate machine and BSP-specific directories.

## Layer Structure

Typical contents include:

```text
meta-rdkb-bsp/
├── conf/
│   ├── layer.conf
│   └── machine/
├── recipes-core/
│   └── images/
├── recipes-bsp/
├── recipes-kernel/
└── recipes-support/
```

## Building the Vanilla Image

The vanilla baseline uses Yocto Project 6 (Wrynose), pure OpenEmbedded-Core
defaults (`nodistro`, with systemd), and the upstream `qemuarm64` machine.
`rdkb-bsp-vanilla-image` installs the standard `packagegroup-core-boot`
packagegroup. This layer adds no RDK-B runtime packages or custom distro or
machine configuration.

### Host prerequisites

Use a supported Linux host with at least 140 GB of free disk space and 32 GB
of RAM. On Ubuntu 24.04, install the build dependencies:

```sh
sudo apt-get install build-essential chrpath cpio debianutils diffstat file \
    gawk gcc git iputils-ping libacl1 libcrypt-dev locales python3 python3-git \
    python3-jinja2 python3-pexpect python3-pip python3-subunit socat texinfo \
    unzip wget xz-utils zstd
```

Ensure `locale -a` lists `en_US.utf8`. Run the build as a regular user.
See the [Yocto 6 host requirements](https://docs.yoctoproject.org/6.0/ref-manual/system-requirements.html)
for other supported distributions.

### Set up the environment

Run these commands in Bash from the root of this repository. Both upstream
repositories use the same release tag, `yocto-6.0.3`. Their checkouts, build
output, downloads, and shared-state cache stay under the ignored
`build-qemu/` directory.

```sh
repo_root="$PWD"
mkdir -p "$repo_root/build-qemu/layers"
git clone --depth 1 --branch yocto-6.0.3 \
    https://git.openembedded.org/bitbake \
    "$repo_root/build-qemu/layers/bitbake"
git clone --depth 1 --branch yocto-6.0.3 \
    https://git.openembedded.org/openembedded-core \
    "$repo_root/build-qemu/layers/openembedded-core"
source "$repo_root/build-qemu/layers/openembedded-core/oe-init-build-env" \
    "$repo_root/build-qemu"
bitbake-layers add-layer "$repo_root"
```

In the generated `build-qemu/conf/local.conf`, set the machine as follows
and keep the other OE-Core defaults:

```bitbake
MACHINE = "qemuarm64"
```

For later sessions, reopen the environment from the repository root:

```sh
repo_root="$PWD"
source "$repo_root/build-qemu/layers/openembedded-core/oe-init-build-env" \
    "$repo_root/build-qemu"
```

### Visual Studio Code

Open this repository root as the VS Code folder. Install the recommended
**Yocto Project BitBake** extension (`yocto-project.yocto-bitbake`). The
checked-in `.vscode/settings.json` points the extension at `build-qemu/`
and the upstream sources under `build-qemu/layers/`.

Use **Ctrl+Shift+P → Preferences: Open Workspace Settings (JSON)** to view
or edit these settings. Use **Ctrl+Shift+P → BitBake: Open BitBake terminal**
to open a terminal with the build environment initialized. The editor
stays at the repository root while that terminal works in `build-qemu/`.

Automatic parsing on save is disabled; invoke parsing explicitly when
ready. Build output and caches are excluded from file watching and search.
The `tmp/` directory and images are generated when a build runs.

### Resource-limited Docker builds

Build the Ubuntu 24.04 Yocto host image once:

```sh
./scripts/docker-build-image
```

Open an interactive build shell in a container limited to 8 CPUs and 24 GB
of memory:

```sh
./scripts/docker-bitbake
```

The shell opens in `build-qemu/` with the OpenEmbedded environment sourced.
Build the minimal upstream image with `bitbake core-image-minimal`, or build
this layer's image with `bitbake rdkb-bsp-vanilla-image`.

Commands can also be run directly:

```sh
./scripts/docker-bitbake -- bitbake core-image-minimal
```

The repository is mounted into the container at the same absolute path, so
the existing downloads, shared-state cache, and completed tasks are reused.
The container runs with the host user's UID and GID, keeping generated files
owned by the current user. Network access uses Docker's default bridge.

Override the default limits when needed:

```sh
YOCTO_CONTAINER_CPUS=4 YOCTO_CONTAINER_MEMORY=12g \
    ./scripts/docker-bitbake -- bitbake core-image-minimal
```

The VS Code BitBake extension uses this wrapper through the checked-in
workspace settings. Reload VS Code after building the container image, then
run **BitBake: Rescan Project** while no other BitBake command is active.

### Validate and build when ready

After initializing the environment, inspect the layers and parse the metadata:

```sh
bitbake-layers show-layers
bitbake-layers show-recipes rdkb-bsp-vanilla-image
bitbake -p
```

Start the image build separately:

```sh
bitbake rdkb-bsp-vanilla-image
```

Images are written to `build-qemu/tmp/deploy/images/qemuarm64/`. To boot
the resulting image with a serial console and user-mode networking:

```sh
runqemu qemuarm64 rdkb-bsp-vanilla-image nographic slirp
```

This uses the upstream login policy; enabling an interactive root login is
a separate development configuration choice. For background on the separate
upstream repositories, see the [Yocto 6 manual setup guide](https://docs.yoctoproject.org/6.0/dev-manual/poky-manual-setup.html).
