BUILD=debug
#BUILD=release

ADB=adb

all: build

sdk.dir:
	@echo
	@echo "Please provide the path to the Android SDK:"
	@echo "  ln -fs ~/android-sdk/sdk sdk.dir"
	@echo
	@echo "And then run:"
	@echo "  make BUILD=debug"
	@echo "  make BUILD=release"
	@echo
	@echo "To make it suid on a rooted device:"
	@echo "  make suid"
	@echo
	@false

build:
	ant ${BUILD}

clean: sdk.dir
	rm -rf bin gen
	rm -f org.radare2.installer.apk

uninstall:
	$(ADB) shell 'LD_LIBRARY_PATH=/system/lib pm uninstall org.radare2.installer'


install: uninstall build
	$(ADB) install bin/radare2-installer-${BUILD}.apk
	$(ADB) shell 'LD_LIBRARY_PATH=/system/lib am start -n org.radare2.installer/.LaunchActivity'

test:
	$(ADB) shell "su -c 'LD_LIBRARY_PATH=/system/lib pm uninstall org.radare2.installer'"
	#$(ADB) shell "su -c 'LD_LIBRARY_PATH=/system/lib pm uninstall jackpal.androidterm'"
	rm -rf bin gen
	ant ${BUILD} install
	$(ADB) shell rm /sdcard/radare2-installer-${BUILD}.apk
	$(ADB) push bin/radare2-installer-${BUILD}.apk /sdcard/
	$(ADB) shell "su -c 'LD_LIBRARY_PATH=/system/lib pm install /sdcard/radare2-installer-${BUILD}.apk'"
	$(ADB) shell 'LD_LIBRARY_PATH=/system/lib am start -n org.radare2.installer/.LaunchActivity'

key.store:
	keytool -genkey -v -keystore key.store -alias Radare2 \
		-keyalg RSA -keysize 4096 -validity 100000

sign:
	cp bin/radare2-installer-release-unsigned.apk bin/radare2-installer-release.apk
	jarsigner -verbose -keystore key.store \
		-digestalg SHA1 -sigalg MD5withRSA \
		bin/radare2-installer-release.apk Radare2
	rm -f bin/radare2-installer-final.apk
	sdk.dir/tools/zipalign 4 bin/radare2-installer-release.apk \
		org.radare2.installer.apk

align:
	sdk.dir/tools/zipalign 4 bin/radare2-installer-${BUILD}.apk bin/radare2-installer-aligned.apk

R2BIN=/data/data/org.radare2.installer/radare2/bin/radare2
suid:
	$(ADB) shell chown root:root ${R2BIN}
	$(ADB) shell chmod 4755 ${R2BIN}
	

.PHONY: test install uninstall build
