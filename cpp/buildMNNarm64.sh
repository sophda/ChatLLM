cd /home/sophda/llmprojects/ChatRobot/MNN/arm64
rm -rf ./*

cmake \
    -DCMAKE_TOOLCHAIN_FILE=${NDK27}/build/cmake/android.toolchain.cmake \
    -DCMAKE_BUILD_TYPE=Release \
    -DANDROID_ABI="arm64-v8a" \
    -DANDROID_STL=c++_static \
    -DMNN_USE_LOGCAT=false \
    -DMNN_BUILD_BENCHMARK=ON \
    -DMNN_USE_SSE=OFF \
    -DMNN_BUILD_TEST=ON \
    -DANDROID_NATIVE_API_LEVEL=android-21  \
    -DMNN_BUILD_FOR_ANDROID_COMMAND=true \
    ..


# cmake \
#     -DCMAKE_TOOLCHAIN_FILE=${NDK27}/build/cmake/android.toolchain.cmake \
#     -DANDROID_PLATFORM=android-30 \
# 	-DANDROID_ABI="armeabi-v7a" \
#     ..

make -j20
# ./ChatRobot