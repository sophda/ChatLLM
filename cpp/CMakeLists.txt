cmake_minimum_required(VERSION 3.14)
set(CMAKE_C_COMPILER "clang")
set(CMAKE_CXX_COMPILER "clang++")
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)




project(ChatRobot)
# set(MNN_BUILD_LLM ON)
# add_subdirectory(
#     ${CMAKE_CURRENT_SOURCE_DIR}/MNN/ 
# )


# 添加源文件
file(GLOB SOURCES *.cpp)


# x86_64
# link_directories(
# /home/sophda/llmprojects/ChatRobot/arm64build/MNN/express/
# /home/sophda/llmprojects/ChatRobot/arm64build/MNN)

link_directories(
    /home/sophda/llmprojects/ChatRobot/arm64lib
)

add_library(ChatRobot SHARED ${SOURCES})


target_include_directories(
    ChatRobot PRIVATE
    /home/sophda/llmprojects/ChatRobot/MNN/include
    /home/sophda/llmprojects/ChatRobot/MNN/transformers/llm/engine/include)

# 如有需要可添加 include 目录
# target_include_directories(ChatRobot PRIVATE include)

# 如有需要可链接库
# target_link_libraries(ChatRobot PRIVATE libllm.so libMNN.so libMNN_Express.so    )
target_link_libraries(ChatRobot PRIVATE libllm.so libMNN.so libMNN_Express.so)