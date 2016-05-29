---
--
function dumpPack(s)
	local block = 16
	local i = 1
	while true do
		local bytes = s:sub(i, i+15)
		for _, b in ipairs { string.byte(bytes, 1, -1) } do
			io.write(string. format('%02X ', b))
		end

		io.write(string.rep('   ', block - string.len(bytes)))
		io.write(' ', string.gsub(bytes, '%c', '.'), '\n')

		i = i + 16
		if i > #s then break end
	end
end

---
--
function Bit8toStr(i)
	return string.char(i)
end

---
--
function Bit32toStr(i)
	if i < 0 then return "" end
	local i1 = math.floor(i % 256)
	i = math.floor((i - i1) / 256)
	local i2 = math.floor(i % 256)
	i = math.floor((i - i2) / 256)
	local i3 = math.floor(i % 256)
	i = math.floor((i - i3) / 256)
	local i4 = math.floor(i % 256)
	i = math.floor((i - i4) / 256)
	return string.char(i4, i3, i2, i1)
end

---
--
function Bit16toStr(i)
	if i < 0 then return "" end
	local i1 = math.floor(i % 256)
	i = math.floor((i - i1) / 256)
	local i2 = math.floor(i % 256)
	i = math.floor((i - i2) / 256)
	return string.char(i2, i1)
end

---
--
function RawtoPack(bytes)
	if type(bytes) ~= "string" then return nil end
	return "CT"..Bit32toStr(#bytes)..bytes
end

-- TType
local STOP = 0;
local VOID = 1;
local BOOL = 2;
local BYTE = 3;
local DOUBLE = 4;
local I16 = 6;
local I32 = 8;
local I64 = 10;
local STRING = 11;
local STRUCT = 12;
local MAP = 13;
local SET = 14;
local LIST = 15;
local ENUM = 16;

---
--
local function writethriftstring(field_number, s)
	local str = ""
	str = str..Bit8toStr(STRING)
	str = str..Bit16toStr(field_number)
	str = str..Bit32toStr(string.len(s))
	str = str..s
	return str
end

---
--
local function writethriftint32(field_number, i)
	local str = ""
	str = str..Bit8toStr(I32)
	str = str..Bit16toStr(field_number)
	str = str..Bit32toStr(i)
	return str
end

---
--
local function writethriftstruct(field_number, data)
	local str = ""
	str = str..Bit8toStr(STRUCT)
	str = str..Bit16toStr(field_number)
	str = str..data
	str = str..string.char(0, 0)
	return str
end


---
-- sample package generater
--
local MODL_GAMECLIENT = 5
local NMSG_LOGIN = 1
local NMSG_MOVE = 3
local MODL_SERIALIZER = 0xff
local THRIFT = 2

-- Big Endian
function ThriftPack1(user, pass)
	print("ThriftPack1")
	local bytes = ""
	local bytes2 = ""
	local bytes3 = ""
	local packtype = 1
	bytes3 = bytes3..writethriftstring(1, user)
	bytes3 = bytes3..writethriftstring(2, pass)
	--
	bytes2 = bytes2..writethriftstruct(packtype, bytes3)
	--
	bytes = bytes..Bit8toStr(MODL_SERIALIZER)
	bytes = bytes..Bit8toStr(THRIFT)
	bytes = bytes..bytes2
	bytes = RawtoPack(bytes)
	dumpPack(bytes)
	return bytes
end

-- Big Endian
function ThriftPack2(user, x, y)
	print("ThriftPack2")
	local bytes = ""
	local bytes2 = ""
	local bytes3 = ""
	local packtype = 2
	bytes3 = bytes3..writethriftstring(1, user)
	bytes3 = bytes3..writethriftint32(2, x)
	bytes3 = bytes3..writethriftint32(3, y)
	--
	bytes2 = bytes2..writethriftstruct(packtype, bytes3)
	--
	bytes = bytes..Bit8toStr(MODL_SERIALIZER)
	bytes = bytes..Bit8toStr(THRIFT)
	bytes = bytes..bytes2
	bytes = RawtoPack(bytes)
	dumpPack(bytes)
	return bytes
end

local function test()
	ThriftPack1("nanami", "12345")
	ThriftPack2("nanami", 1, 2)
end

--test()

--
---
-- Pack socket test 02
--
local socket = require("socket")
host = host or "localhost"
port = port or 8899
host = socket.dns.toip(host)
print("IP: '" ..host.. "' port: " .. port .. "...")

local connects = {}
--New tcp socket
xpcall(function ()
	for k = 1, 100 do
		local c = assert(socket.tcp())
		assert(c:connect(host, port))
		c:send(ThriftPack1("nanami", "12345")) -- 23 bytes
		c:send(ThriftPack2("nanami", k, 3)) -- 24 bytes
		c:send(ThriftPack1("nanami", "12345")) -- 23 bytes
		if false then
			connects[#connects+1] = c
		else
			c:close()
		end
	end
end, function(message)
	print(message)
	print(debug.traceback());
end)

-- testcase()

print("Ending...")

--prevent data read fail
--os.execute('pause');

