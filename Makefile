BUILD=debug
#BUILD=release
PKGNAME=org.radare.radare2installer
USE_GRADLE=1
HAVE_ZIPALIGN=1
ZIPALIGN=./zipalign
#sdk.dir/tools/zipalign

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
ifeq ($(USE_GRADLE),1)
	gradle build
else
	ant ${BUILD}
endif

clean:
ifeq ($(USE_GRADLE),1)
	gradle clean
else
	ant clean
endif
	rm -rf bin gen
	rm -f $(PKGNAME).apk

uninstall:
	adb shell pm uninstall org.radare.radare2installer
	#$(ADB) shell 'LD_LIBRARY_PATH=/system/lib pm uninstall $(PKGNAME)'

# build
install: uninstall
	adb install org.radare.radare2installer.apk
#	$(ADB) install bin/$(PKGNAME)-${BUILD}.apk
	$(ADB) shell 'LD_LIBRARY_PATH=/system/lib64 am start -n $(PKGNAME)/.LaunchActivity'

dist release: sdk.dir
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
APK_UNSIGNED=build/outputs/apk/radare2-installer-release-unsigned.apk
sign:
	mkdir -p bin
	sh apk-clean.sh $(APK_UNSIGNED) bin/$(APPNAME)-release.apk
	jarsigner -verbose -keystore key.store -digestalg SHA1 -sigalg MD5withRSA \
		bin/$(APPNAME)-release.apk Radare2
	rm -f $(PKGNAME).apk
ifeq ($(HAVE_ZIPALIGN),1)
	$(ZIPALIGN) 4 bin/$(APPNAME)-release.apk $(PKGNAME).apk
endif

align:
	$(ZIPALIGN) 4 bin/$(APPNAME)-${BUILD}.apk bin/$(APPNAME)-aligned.apk

R2BIN=/data/data/$(PKGNAME)/radare2/bin/radare2
suid:
	$(ADB) shell chown root:root ${R2BIN}
	$(ADB) shell chmod 4755 ${R2BIN}
	
.PHONY: test install uninstall build
