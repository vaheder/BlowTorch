
Color = luajava.bindClass("com.offsetnull.bt.ui.ColorCompat")
Path = luajava.bindClass("android.graphics.Path")
PathDirection = luajava.bindClass("android.graphics.Path$Direction")
statusoffset = 0

--typeface support for bolding text
local Typeface = luajava.bindClass("android.graphics.Typeface")
local DEFAULT_BOLD_TYPEFACE = Typeface.DEFAULT_BOLD

buttonRoundness = 16
BUTTONSET_DATA = {
						height 			= 48,
						width 			= 48,
						labelSize 		= 16,
						primaryColor 	= Color:argb(0x88,0x00,0x00,0xFF),
						labelColor		= Color:argb(0xAA,0xAA,0xAA,0xAA),
						selectedColor 	= Color:argb(0x88,0x00,0xFF,0x00),
						flipColor 		= Color:argb(0x88,0xFF,0x00,0x00),
						flipLabelColor 	= Color:argb(0x88,0x00,0x00,0xFF),
						command = "",
						label = "LABEL",
						flipLabel = "",
						flipCommand = "",
						name = "",
						switchTo = "",
						gridXwidth = 50,
						gridYwidth = 50			
			  		}
function BUTTONSET_DATA:new(o)
	o = o or {}
	setmetatable(o,self)
	return o
end

BUTTONSET_DATA.__index = BUTTONSET_DATA

BUTTON_DATA = 	 { 	
						x				= 100,
						y				= 100,
						--height 			= 80,
						--width 			= 80,
						--command 		= "",
						--label 			= "LABEL",
						--labelSize 		= 23,
						--flipLabel		= "",
						--flipCommand 	= "",
						--primaryColor 	= Color:argb(0x88,0x00,0x00,0xFF),
						--labelColor		= Color:argb(0xAA,0xAA,0xAA,0xAA),
						--selectedColor 	= Color:argb(0x88,0x00,0xFF,0x00),
						--flipColor 		= Color:argb(0x88,0xFF,0x00,0x00),
						--flipLabelColor 	= Color:argb(0x88,0x00,0x00,0xFF)				
			  	 }
function BUTTON_DATA:new(o)
	o = o or {}
	setmetatable(o,self)
	return o
end

BUTTON_DATA.__index = BUTTONSET_DATA

BUTTON = {} -- this class is purley a factory. these represent "in use" buttons
function BUTTON:new(data,density)
	local o = {}
	o.paintOpts = luajava.newInstance("android.graphics.Paint")
	o.paintOpts:setAntiAlias(true)
	o.paintOpts:setXfermode(xferModeSRC)
	o.paintOpts:setTypeface(DEFAULT_BOLD_TYPEFACE)
	o.rect = luajava.newInstance("android.graphics.RectF")
	o.inset = luajava.newInstance("android.graphics.RectF")
	o.data = BUTTON_DATA:new(data)
	o.selected = false
	setmetatable(o,self)
	self.__index = self
	o.density = density
	o:updateRect(statusoffset)
	
	return o
end

function BUTTON:updateRect(statusoffset)
	local left = self.data.x - (self.data.width/2)*self.density
	local right = self.data.x + (self.data.width/2)*self.density
	local top = self.data.y - (self.data.height/2)*self.density + statusoffset
	local bottom = self.data.y + (self.data.height/2)*self.density + statusoffset
	local tmp = self.rect

	tmp:set(left,top,right,bottom)
	self.inset:set(left+1.0,top+1.0,right-1.0,bottom-1.0)
end

function BUTTON:draw(state,canvas)
	if(canvas == nil) then
		error("canvas parameter must not be null")
	end
	
	local usestate = 0
	local p = self.paintOpts
	if(state ~= nil) then
		usestate = state
	end
	
	local rect = self.rect
	--Note("drawing button, roundness is"..buttonRoundness)
	--buttonRoundness = 30.0
	if(usestate == 0) then
		p:setColor(self.data.primaryColor)
		canvas:drawRoundRect(rect,buttonRoundness,buttonRoundness,p)
	elseif(usestate == 1) then
		p:setColor(self.data.selectedColor)
		canvas:drawRoundRect(self.inset,buttonRoundness,buttonRoundness,p)
	elseif(usestate == 2) then
		p:setColor(self.data.flipColor)
		canvas:drawRoundRect(self.inset,buttonRoundness,buttonRoundness,p)
	end
	
	local label = nil
	if(usestate == 0 or usestate == 1) then
		p:setColor(self.data.labelColor)
		p:setTextSize(tonumber(self.data.labelSize)*self.density)
		--p:setTypeface(DEFAULT_BOLD_TYPEFACE)
		label = self.data.label
	elseif(usestate == 2) then
		p:setColor(self.data.flipLabelColor)
		--p:setTypeface(DEFAULT_BOLD_TYPEFACE)
		if(self.data.flipLabel == "" or self.data.flipLabel == nil) then
			label = self.data.label
		else
			label = self.data.flipLabel
		end
		
	end
	local tX = self.data.x - (p:measureText(label)/2)
	local tY = self.data.y + (p:getTextSize()/2) + statusoffset
	p:setTypeface(DEFAULT_BOLD_TYPEFACE)
	canvas:drawText(label,tX,tY,p)
end

PorterDuffMode = luajava.bindClass("android.graphics.PorterDuff$Mode")
xferModeClear = luajava.newInstance("android.graphics.PorterDuffXfermode",PorterDuffMode.CLEAR)
xferModeSRC = luajava.newInstance("android.graphics.PorterDuffXfermode",PorterDuffMode.SRC)

function BUTTON:clearButton(canvas)
	local p = self.paintOpts
	p:setXfermode(xferModeClear)
	canvas:drawRoundRect(self.rect,5,5,p)
	p:setXfermode(nil)
end
