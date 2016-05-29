local function packet(structure)
	return function(self, data)
		assert(type(self) == "table" and type(data) == "table",
			"please check schema:xxx(){...} call format")
		assert(#structure == #data + 1,
			"please check schema:xxx(){...} call number")
		--print("structure:", unpack(structure))
		--print("data:", unpack(data))
		return ""
	end
end

local ser = {
	login = packet{"ss", "username", "password"},
	move = packet{"sii", "username", "x", "y"},
}

local data = {}
data[#data + 1] = ser:login{"nanami", "12345"}
data[#data + 1] = ser:move{"nanami", 1, 2}
data[#data + 1] = ser:login{"nanami", "12345"}
