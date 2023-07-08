#Application.mk
#设置NDK库函数版本号，一般和Android版本号对应
APP_PLATFORM = android-30
#设置需要编译的CPU类型，这里只编译armeabi-v7a和arm64-v8a两种，使用all可以编译所有类型
APP_ABI := all
#设置以静态链接方式连接C++标准库
APP_STL := c++_static
#设置编译版本，debug版本附带调试信息，支持gdb-server断点调试，release版本不带调试信息
APP_OPTIM := release