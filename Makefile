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
	@false

build: clean
	ant ${BUILD}

clean: sdk.dir
	rm -rf bin gen

uninstall:
	$(ADB) shell 'LD_LIBRARY_PATH=/system/lib pm uninstall org.radare.installer'


install: uninstall build
	$(ADB) install bin/radare2-installer-${BUILD}.apk
	$(ADB) shell 'LD_LIBRARY_PATH=/system/lib am start -n org.radare.installer/.LaunchActivity'

test:
	$(ADB) shell "su -c 'LD_LIBRARY_PATH=/system/lib pm uninstall org.radare.installer'"
	#$(ADB) shell "su -c 'LD_LIBRARY_PATH=/system/lib pm uninstall jackpal.androidterm'"
	rm -rf bin gen
	ant ${BUILD} install
	$(ADB) shell rm /sdcard/radare2-installer-${BUILD}.apk
	$(ADB) push bin/radare2-installer-${BUILD}.apk /sdcard/
	$(ADB) shell "su -c 'LD_LIBRARY_PATH=/system/lib pm install /sdcard/radare2-installer-${BUILD}.apk'"
	$(ADB) shell 'LD_LIBRARY_PATH=/system/lib am start -n org.radare.installer/.LaunchActivity'
	

.PHONY: test install uninstall build
