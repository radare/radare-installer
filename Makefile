BUILD=debug
#BUILD=release
PKGNAME=org.radare.radare2installer

ADB=adb

all: build

sdk.dir:
	@echo
	@echo "Please provide the path to the Android SDK:"
	@echo "  ln -fs ~/android-sdk/sdk sdk.dir"
	@echo
	@echo "On macOS / OSX:
	@echo "  ln -fs ~/Library/Android/sdk sdk.dir"
	@echo
	@echo "And then run:"
	@echo "  make                  # debug build"
	@echo "  make release          # release sign and align"
	@echo "  make install-release  # install via adb"
	@echo
	@echo "To make it suid on a rooted device:"
	@echo "  make suid"
	@echo
	@false

build: sdk.dir
	ant ${BUILD}

clean:
	ant clean
	rm -rf bin gen
	rm -f $(PKGNAME).apk

uninstall:
	$(ADB) shell 'LD_LIBRARY_PATH=/system/lib pm uninstall $(PKGNAME)'


install: uninstall build
	$(ADB) install bin/$(PKGNAME)-${BUILD}.apk
	$(ADB) shell 'LD_LIBRARY_PATH=/system/lib am start -n $(PKGNAME)/.LaunchActivity'

release: sdk.dir
	ant release
	$(MAKE) sign

release-install install-release: uninstall release
	$(ADB) install $(PKGNAME).apk
	$(ADB) shell 'LD_LIBRARY_PATH=/system/lib am start -n $(PKGNAME)/.LaunchActivity'

test:
	$(ADB) shell "su -c 'LD_LIBRARY_PATH=/system/lib pm uninstall $(PKGNAME)'"
	#$(ADB) shell "su -c 'LD_LIBRARY_PATH=/system/lib pm uninstall jackpal.androidterm'"
	rm -rf bin gen
	ant ${BUILD} install
	$(ADB) shell rm /sdcard/$(PKGNAME)-${BUILD}.apk
	$(ADB) push bin/$(PKGNAME)-${BUILD}.apk /sdcard/
	$(ADB) shell "su -c 'LD_LIBRARY_PATH=/system/lib pm install /sdcard/$(PKGNAME)-${BUILD}.apk'"
	$(ADB) shell 'LD_LIBRARY_PATH=/system/lib am start -n $(PKGNAME)/.LaunchActivity'

key.store:
	keytool -genkey -v -keystore key.store -alias Radare2 \
		-keyalg RSA -keysize 4096 -validity 100000

APPNAME=radare2installer
sign:
	sh apk-clean.sh bin/$(APPNAME)-release-unsigned.apk bin/$(APPNAME)-release.apk
	jarsigner -verbose -keystore key.store -digestalg SHA1 -sigalg MD5withRSA \
		bin/$(APPNAME)-release.apk Radare2
	rm -f $(PKGNAME).apk
	sdk.dir/tools/zipalign 4 bin/$(APPNAME)-release.apk $(PKGNAME).apk

align:
	sdk.dir/tools/zipalign 4 bin/$(APPNAME)-${BUILD}.apk bin/$(APPNAME)-aligned.apk

R2BIN=/data/data/$(PKGNAME)/radare2/bin/radare2
suid:
	$(ADB) shell chown root:root ${R2BIN}
	$(ADB) shell chmod 4755 ${R2BIN}
	

.PHONY: test install uninstall build
