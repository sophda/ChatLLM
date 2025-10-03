cd /home/sophda/llmprojects/ChatRobot/arm64build
# rm -rf ./*

cmake \
    -DCMAKE_TOOLCHAIN_FILE=${NDK27}/build/cmake/android.toolchain.cmake \
    -DANDROID_PLATFORM=android-30 \
	-DANDROID_ABI="arm64-v8a" \
    ..


make -j20
# ./ChatRobot