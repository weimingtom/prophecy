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
function RawtoPack(bytes)
	if type(bytes) ~= "string" then return nil end
	return "CT"..Bit32toStr(#bytes)..bytes
end

---
-- see also:
-- http://code.google.com/apis/protocolbuffers/docs/encoding.html
--
local function writepbstring(field_number, s)
	local str = ""
	-- wire_type == 2, wire_type << 3 == 8
	str = str..string.char(field_number * 8 + 2)
	str = str..string.char(string.len(s))
	str = str..s
	return str
end

---
-- see also:
-- http://code.google.com/apis/protocolbuffers/docs/encoding.html
--
local function writepbint32(field_number, i)
	local str = ""
	-- wire_type == 0, wire_type << 3 == 8
	str = str..string.char(field_number * 8 + 0)

	if i < 0 then
		i = i % (256 * 256 * 256 * 256)
	end

	local k = i % (128 * 128 * 128 * 128)
	if k < 128 then
		str = str..string.char(k % 128)
		--print(string.format("0x%0X", k % 128))
		return str
	else
		str = str..string.char(k % 128 + 128)
		--print(string.format("0x%0X", k % 128 + 128))
		k = math.floor(k / 128)
	end
	if k < 128 then
		str = str..string.char(k % 128)
		--print(string.format("0x%0X", k % 128))
		return str
	else
		str = str..string.char(k % 128 + 128)
		--print(string.format("0x%0X", k % 128 + 128))
		k = math.floor(k / 128)
	end
	if k < 128 then
		str = str..string.char(k % 128)
		--print(string.format("0x%0X", k % 128))
		return str
	else
		str = str..string.char(k % 128 + 128)
		k = math.floor(k / 128)
		--print(string.format("0x%0X", k % 128 + 128))
	end
	if k < 128 then
		if k == 127 then
			str = str..string.char(255, 255, 255, 255, 255, 255, 1)
			--print("navigate")
		else
			str = str..string.char(k % 128)
			--print(string.format("0x%0X", k % 128))
		end
		return str
	else
		str = str..string.char(k % 128 + 128)
		k = math.floor(k / 128)
		--print(string.format("0x%0X", k % 128 + 128))
	end
	return str
end

---
-- sample package generater
--
local MODL_SERIALIZER = 0xff
local PROTOSUTFFRUNTIME = 3

-- Big Endian
function ProtostuffRuntime_PBPack1(user, pass)
	print("PBPack1")
	local bytes = ""
	local bytes2 = ""
	local bytes3 = ""
	local pbtype = 1
	bytes3 = bytes3..writepbstring(1, user)
	bytes3 = bytes3..writepbstring(2, pass)
	bytes2 = bytes3
	bytes = bytes..Bit8toStr(MODL_SERIALIZER)
	bytes = bytes..Bit8toStr(PROTOSUTFFRUNTIME)
	bytes = bytes..string.char(1, pbtype)
	bytes = bytes..bytes2
	bytes = RawtoPack(bytes)
	dumpPack(bytes)
	return bytes
end

-- Big Endian
function ProtostuffRuntime_PBPack2(user, x, y)
	print("PBPack2")
	local bytes = ""
	local bytes2 = ""
	local bytes3 = ""
	local pbtype = 2
	bytes3 = bytes3..writepbstring(1, user)
	bytes3 = bytes3..writepbint32(2, x)
	bytes3 = bytes3..writepbint32(3, y)
	bytes2 = bytes3
	bytes = bytes..Bit8toStr(MODL_SERIALIZER)
	bytes = bytes..Bit8toStr(PROTOSUTFFRUNTIME)
	bytes = bytes..string.char(1, pbtype)
	bytes = bytes..bytes2
	bytes = RawtoPack(bytes)
	dumpPack(bytes)
	return bytes
end

local function test()
	ProtostuffRuntime_PBPack1("nanami", "12345")
	ProtostuffRuntime_PBPack2("nanami", 1, 2)
end

--test()

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
	for k = 1, 10000 do
		local c = assert(socket.tcp())
		assert(c:connect(host, port))
		c:send(ProtostuffRuntime_PBPack1("nanami", "12345")) -- 23 bytes
		c:send(ProtostuffRuntime_PBPack2("nanami", k, 3)) -- 24 bytes
		c:send(ProtostuffRuntime_PBPack1("nanami", "12345")) -- 23 bytes
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
