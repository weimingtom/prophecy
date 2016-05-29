namespace java com.ugame.prophecy.serializer.thrift

struct LoginPack {
	1: string username
	2: string password
}

struct MovePack {
	1: string username
	2: i32 x
	3: i32 y
}

struct CommonPack {
	1: optional LoginPack login
	2: optional MovePack move
}
