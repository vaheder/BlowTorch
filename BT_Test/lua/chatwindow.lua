--debugPrint("package path:"..package.path)
--package.path = "/mnt/sdcard/BlowTorch/?.lua"
debugPrint("OH MY FUCKING GOD IT IS LOADING")
--make a button.
respath = GetExternalStorageDirectory().."/BlowTorch/plugins/aardwolf/icons"
--respath = "/mnt/sdcard/BlowTorch/plugins/aardwolf/icons"
--debugPrint("in the chat window")
context = view:getContext()
density = getDisplayDensity()
debugPrint("Density: "..density)
debugPrint("Respath: "..respath)
density = tonumber(density)
System = luajava.bindClass("java.lang.System")
View = luajava.bindClass("android.view.View")
HorizontalScrollView = luajava.bindClass("android.widget.HorizontalScrollView")
Drawable = luajava.bindClass("android.graphics.drawable.Drawable")
RelativeLayoutParams = luajava.bindClass("android.widget.RelativeLayout$LayoutParams")
RelativeLayout = luajava.bindClass("android.widget.RelativeLayout")
LinearLayout = luajava.bindClass("android.widget.LinearLayout")
LinearLayoutParams = luajava.bindClass("android.widget.LinearLayout$LayoutParams")
MotionEvent = luajava.bindClass("android.view.MotionEvent")
Array = luajava.bindClass("java.lang.reflect.Array")
Integer = luajava.newInstance("java.lang.Integer",0)
Color = luajava.bindClass("android.graphics.Color")
File = luajava.bindClass("java.io.File")
IntegerClass = Integer:getClass()
RawInteger = IntegerClass.TYPE

Float = luajava.newInstance("java.lang.Float",0)
FloatClass = Float:getClass()
RawFloat = FloatClass.TYPE

R = luajava.bindClass("android.R$attr")
--LinearLayout = luajava.bindClass("android.widget.LinearLayout")
--LinearLayoutParams = luajava.bindClass("android.widget.LinearLayout$LayoutParams")

--debugPrint("bound necessary classes")

resources = view:getContext():getResources()
function resLoader(root,bmp)
	local target = nil
	local metrics = resources:getDisplayMetrics()
	
	local d = metrics.density
	--debugPrint("resloader density: "..d)
	if(d < 1.0) then
		target = luajava.newInstance("android.graphics.drawable.BitmapDrawable",resources,root.."/ldpi/"..bmp)
	elseif(d >= 1.0 and d < 1.5) then
		testfile = luajava.new(File,root.."/mdpi/"..bmp)
		if(testfile:exists()) then
			debugPrint("file exists")
		else
			debugPrint("file does not exist")
		end
		target = luajava.newInstance("android.graphics.drawable.BitmapDrawable",resources,root.."/mdpi/"..bmp)
	elseif(d >= 1.5) then
		--debugPrint("using hdpi")
		target = luajava.newInstance("android.graphics.drawable.BitmapDrawable",resources,root.."/hdpi/"..bmp)
	end
	
	return target
end


function makeFloatArray(table)
	newarray = Array:newInstance(RawFloat,#table)
	for i,v in ipairs(table) do
		index = i-1
		floatval = luajava.newInstance("java.lang.Float",v)
		Array:setFloat(newarray,index,floatval:floatValue())
	end
	
	return newarray
end

function makeIntArray(table)
	newarray = Array:newInstance(RawInteger,#table)
	for i,v in ipairs(table) do
		index = i-1
		intval = luajava.newInstance("java.lang.Integer",v)
		Array:setInt(newarray,index,intval:intValue())
	end
	
	return newarray
end

function makeEmptyIntArray()
	newarray = Array:newInstance(RawInteger,0)
	return newarray
end
--make holder view for buttons.
--LinearLayout = luajava.bindClass("android.widget.LinearLayout")

startHeight = view:getHeight()
foldoutHeight = 100

uiButtonBarHeight = 35*density
uiButtonBar = luajava.new(RelativeLayout,context)
uiButtonBarParams = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",RelativeLayoutParams.FILL_PARENT,RelativeLayoutParams.WRAP_CONTENT)
uiButtonBarParams:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
--params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
uiButtonBar:setLayoutParams(uiButtonBarParams)
uiButtonBar:setBackgroundColor(Color:argb(255,0,0,0))
uiButtonBar:setVisibility(View.GONE)

scrollHolder = luajava.new(HorizontalScrollView,context)
scrollHolderParams = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",RelativeLayoutParams.FILL_PARENT,RelativeLayoutParams.WRAP_CONTENT)
scrollHolderParams:addRule(RelativeLayout.LEFT_OF,98)
scrollHolderParams:addRule(RelativeLayout.RIGHT_OF,102)
--params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
scrollHolder:setLayoutParams(scrollHolderParams)

channelHolder = luajava.new(LinearLayout,context)
channelHolderParams = luajava.newInstance("android.widget.LinearLayout$LayoutParams",LinearLayoutParams.WRAP_CONTENT,LinearLayoutParams.WRAP_CONTENT)
--params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
channelHolder:setLayoutParams(channelHolderParams)

scrollHolder:addView(channelHolder)
--uiButtonBar:setOrientation(LinearLayout.HORIZONTAL)

--button = luajava.newInstance("android.widget.Button",context)
--params = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)
--params:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
--params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
--debugPrint("created instances")
--button:setText("LUABUTTON")
--button:setTextSize(26)
--button:setLayoutParams(params)
--debugPrint("set settings")

--set up the animations.
expandAnimation = luajava.newInstance("android.view.animation.TranslateAnimation",0,0,-foldoutHeight,0)
shrinkAnimation = luajava.newInstance("android.view.animation.TranslateAnimation",0,0,0,-foldoutHeight)

windowMoveUpAnimation = luajava.newInstance("android.view.animation.TranslateAnimation",0,0,uiButtonBarHeight,0)
windowMoveDownAnimation = luajava.newInstance("android.view.animation.TranslateAnimation",0,0,0,uiButtonBarHeight)

nullAnimation = luajava.newInstance("android.view.animation.TranslateAnimation",0,0,0,0)

expandAnimation:setDuration(450)
shrinkAnimation:setDuration(450)
windowMoveUpAnimation:setDuration(450)
windowMoveDownAnimation:setDuration(450)
nullAnimation:setDuration(450)

toggle = false

buttonMap = {}
drawableMap = {}

hider = {}
function hider.onClick(v)
	if(toggle == true) then
		--debugPrint("shrink");
		mainWindow:setLayoutParams(altmainparams)
		parentView:startAnimation(shrinkAnimation)
		if(pinned == false) then
			linear:startAnimation(windowMoveDownAnimation)
			--divider:startAnimation(windowMoveDownAnimation)
			view:startAnimation(nullAnimation)
		end
		view:setTextSelectionEnabled(false)
		if(previousOnTop ~= nil) then
			previousOnTop:bringToFront()
		end
		toggle = false
	end
end

function onParentAnimationEnd()
	--debugPrint("in onParentAnimationEnd()")
	if(toggle == false) then
		twidth = parentView:getWidth()
		theight = parentView:getHeight()
		debugPrint("parentAnimationEnd:"..twidth..":"..theight)
		theight = theight - foldoutHeight
		
		parentView:setDimensions(twidth,theight)
		
		if(pinned == true) then
			wheight = theight - uiButtonBarHeight
			--view:setHeight(wheight)
			local tmpparams = luajava.new(LinearLayoutParams,viewParams.width,wheight)
			view:setLayoutParams(tmpparams)
			view:requestLayout()
		else 
			uiButtonBar:setVisibility(View.GONE)
		end
		mainWindow:setLayoutParams(mainparams)
		parentView:requestLayout()
		
	else
		mainWindow:setLayoutParams(mainparams)
	end
end

function onAnimationEnd()
	
	debugPrint("in onAnimationEnd()")
	--params = view:getLayoutParams()
	if(toggle == false) then
		if(pinned == false) then
			
			wheight = view:getHeight() + uiButtonBarHeight - foldoutHeight
			debugPrint("shrinking, pinned false "..wheight.." view height:"..view:getHeight())
			local tmp = view:getLayoutParams()
			local p = luajava.new(LinearLayoutParams,tmp.width,wheight);
			view:setLayoutParams(p)
			--divider:setLayoutParams(altdividerparams)
		else
			debugPrint("shrinking, pinned true")
			wheight = view:getHeight() - uiButtonBarHeight
			local tmp = view:getLayoutParams()
			local p = luajava.new(LinearLayoutParams,tmp.width,wheight);
			view:setLayoutParams(p)
		end
	end
	--view:setLayoutParams(params)
	view:requestLayout()
	view:invalidate()
end

hider_cb = luajava.createProxy("android.view.View$OnClickListener",hider)
--button:setOnClickListener(toggler_cb)


debugPrint("HOLY FUCKING GOD WE MADE IT HERE")


minHeight = view:getLayoutParams().height + 3*getDisplayDensity()

rootView = view:getParentView()
rootView:removeView(view)

parentView = luajava.newInstance("com.offsetnull.bt.window.AnimatedRelativeLayout",view:getContext())
parentView:setAnimationListener(view)
viewParams = view:getLayoutParams()
chatWindowParams = luajava.new(RelativeLayoutParams,viewParams.width,viewParams.height)
viewParams = luajava.new(RelativeLayoutParams,viewParams.width,RelativeLayoutParams.WRAP_CONTENT)


parentView:setLayoutParams(viewParams)
view:setLayoutParams(chatWindowParams)


mId = view:getId()
parentView:setId(mId)
view:setId(59595)

--make divider
divider = luajava.new(View,view:getContext())
dividerparams = luajava.new(LinearLayoutParams,LinearLayoutParams.FILL_PARENT,3*getDisplayDensity())
--dividerparams:addRule(RelativeLayout.BELOW,view:getId())
divider:setId(59596)
divider:setLayoutParams(dividerparams)
divider:setBackgroundColor(Color:argb(255,68,68,136))



linear = luajava.new(LinearLayout,view:getContext())
linearparams = luajava.new(RelativeLayoutParams,viewParams.width,RelativeLayoutParams.WRAP_CONTENT)
linear:setOrientation(LinearLayout.VERTICAL)
linear:setLayoutParams(linearparams)
linear:setId(59597)

linear:addView(view)
linear:addView(divider)
debugPrint("view has tag: " .. view:getTag())


parentView:addView(uiButtonBar)
parentView:addView(linear);
--parentView:addView(divider)
--parentView:addView(view)
rootView:addView(parentView)


mainWindow = rootView:findViewById(6666)
mainparams = mainWindow:getLayoutParams()
mainparams:addRule(RelativeLayout.BELOW,parentView:getId())
mainparams:addRule(RelativeLayout.ABOVE,40)

altmainparams = luajava.new(RelativeLayoutParams,mainparams.width,mainparams.height)
altmainparams:addRule(RelativeLayout.BELOW,parentView:getId())
altmainparams:addRule(RelativeLayout.ABOVE,40)

mainWindow:setLayoutParams(mainparams)

--rootView:addView(divider)

toucher = {}
function toucher.onLongClick(v)
	if(toggle == true) then
		--consume, but dont process.
		return true;
	end
	--debugPrint("longpressed")
	--debugPrint("grow");
	theight = parentView:getHeight()
	twidth = parentView:getWidth()
	theight = theight + foldoutHeight
	parentView:setDimensions(tonumber(twidth),tonumber(theight))
	
	--fuckheight = luajava.newInstance("java.lang.Integer",theight)
	--fuckwidth = luajava.newInstance("java.lang.Integer",twidth)
	--params = view:getLayoutParams()
	--params.height = theight
	--view:requestLayout()
	--view:updateDimensions(fuckwidth:intValue(),fuckheight:intValue())
	altmainparams = luajava.new(RelativeLayoutParams,mainparams.width,mainWindow:getHeight())
	altmainparams:addRule(RelativeLayout.ABOVE,40)
	
	mainWindow:setLayoutParams(altmainparams)
	
	
	local numchildren = tonumber(rootView:getChildCount())
	previousOnTop = rootView:getChildAt(numchildren-1)
	rootView:bringChildToFront(parentView)
	parentView:startAnimation(expandAnimation)
	toggle = true
	if(pinned == false) then
		wheight = view:getHeight() + foldoutHeight - uiButtonBarHeight
		tmp = view:getLayoutParams();
		local p = luajava.new(LinearLayoutParams,tmp.width,wheight);
		view:setLayoutParams(p)
		linear:startAnimation(windowMoveUpAnimation)
		
		uiButtonBar:setVisibility(View.VISIBLE)
	else
		wheight = view:getHeight() + foldoutHeight
		--view:setHeight(wheight)
		local tmpparams = luajava.new(LinearLayoutParams,viewParams.width,wheight)
		view:setLayoutParams(tmpparams)
	end
	view:requestLayout()
	parentView:requestLayout()
	return true;
end
--toucher_cb = luajava.createProxy("android.view.View$OnLongClickListener",toucher)
--view:setOnLongClickListener(toucher_cb)

toucher2 = {}
moveDelta = 0
moveTotal = 0
moveLast = 0
function toucher2.onTouch(v,e)
	action = e:getAction()
	if(action == MotionEvent.ACTION_DOWN) then
		scheduleCallback(100,"doExpand",1000)
		moveLast = e:getY()
		
		v:onTouchEvent(e)
	elseif( action == MotionEvent.ACTION_MOVE) then
		moveDelta = e:getY() - moveLast
		moveLast = e:getY()
		moveTotal = moveTotal + moveDelta
		if(moveTotal > 30) then
			cancelCallback(100)
		end
		v:onTouchEvent(e)
	elseif(action == MotionEvent.ACTION_UP) then
		v:onTouchEvent(e)
		cancelCallback(100)
	end
	return true
end
toucher2_cb = luajava.createProxy("android.view.View$OnTouchListener",toucher2)
view:setOnTouchListener(toucher2_cb)
view:setTextSelectionEnabled(false)

function doExpand(id)
	toucher.onLongClick(nil)
	view:setTextSelectionEnabled(true)
end


function loadButtons(input)
	uiButtonBar:removeAllViews()
	channelHolder:removeAllViews()
	
	--add the default buttons.
	uiButtonBar:addView(hideButton)
	uiButtonBar:addView(pinButton)
	uiButtonBar:addView(mainButton)
	
	uiButtonBar:addView(scrollHolder)
	
	uiButtonBar:addView(resizeButton)
	uiButtonBar:addView(delButton)
	
	--reconstruct the channel list table
	list = loadstring(input)()
	counter = 1
	lastbutton = nil
	--hasbuttons = false
	for i,b in pairs(list) do
		if(i ~= "main") then
			newbutton = generateNewButton(i,counter)
			params = newbutton:getLayoutParams()
			params:setMargins(2,0,2,0)
			counter = counter + 1
			buttonMap[i] = newbutton
			channelHolder:addView(newbutton)
			lastbutton = newbutton
		end
		--hasbuttons = true
	end
	
	if(lastbutton ~= nil) then
		lparams = lastbutton:getLayoutParams()
		lparams:setMargins(0,0,2,0)
		uiButtonBar:requestLayout()
	end
end

function generateNewButton(name,grav)
	button = luajava.newInstance("android.widget.Button",context)
	params = luajava.newInstance("android.widget.LinearLayout$LayoutParams",LinearLayoutParams.WRAP_CONTENT,uiButtonBarHeight)
	--params:setGravity(grav)
	if(grav == 1) then
		--params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
		button:setId(grav)
	else
		--params:addRule(RelativeLayout.LEFT_OF,grav-1)
		button:setId(grav)
	end
	--params:addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
	--params:addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
	--debugPrint("created instances")
	button:setText(name)
	button:setTextSize(12*density)
	button:setLayoutParams(params)
	button:setOnClickListener(clicker_cb)
	button:setOnLongClickListener(longclicker_cb)
	button:setTextColor(Color:argb(255,150,150,150))
	--button:setPadding(10,0,10,0)
	shape = makeStateDrawable(name)
	
	button:setBackgroundDrawable(shape)
	
	return button
end

local cornerRadii = {}
table.insert(cornerRadii,0)
table.insert(cornerRadii,0)
table.insert(cornerRadii,0)
table.insert(cornerRadii,0)
table.insert(cornerRadii,8)
table.insert(cornerRadii,8)
table.insert(cornerRadii,8)
table.insert(cornerRadii,8)
	
local radii = makeFloatArray(cornerRadii)

local inset = luajava.newInstance("android.graphics.RectF")
inset:set(5,0,5,5)
	
function makeTabDrawable(label,a,r,g,b)
--makes a new shape drawable.
	--start with the 8 floats to make the outer corner

	
	local rectShape = luajava.newInstance("android.graphics.drawable.shapes.RoundRectShape",radii,nil,nil)
	
	local shapeDrawable = luajava.newInstance("android.graphics.drawable.ShapeDrawable",rectShape)
	drawableMap[label] = shapeDrawable

	
	local insetShape = luajava.newInstance("android.graphics.drawable.shapes.RoundRectShape",radii,inset,radii)
	
	local insetDrawable = luajava.newInstance("android.graphics.drawable.ShapeDrawable",insetShape)
	local ipaint = insetDrawable:getPaint()
	ipaint:setARGB(255,150,150,150)
	
	--shapeDrawable:setPadding(10,0,10,0)
	insetDrawable:setPadding(10,0,10,0)
	local layers  = Array:newInstance(Drawable,2)
	Array:set(layers,0,shapeDrawable)
	Array:set(layers,1,insetDrawable)
	
	local layer = luajava.newInstance("android.graphics.drawable.LayerDrawable",layers)
	
	
	local rPaint = shapeDrawable:getPaint()
	rPaint:setARGB(a,r,g,b)
	--shapeDrawable:setBounds(0,0,
	return layer

end

function makeStateDrawable(label)
	local stater = luajava.newInstance("android.graphics.drawable.StateListDrawable")
	local pre = makeTabDrawable(label,255,100,0,0)
	--prePaint = pre:getPaint()
	--prePaint:setARGB(225,100,0,0)
	
	local norm = makeTabDrawable(label,255,0,0,100)
	--npaint = norm:getPaint()
	--npaint:setARGB(225,0,0,100)
	
	--selectDrawable = makeTabDrawable(label)
	--selpaint = selectDrawable:getPaint()
	--selpaint:setARGB(225,200,200,200)
	
	local pretmp = {}
	table.insert(pretmp,R.state_pressed)
	local pressed = makeIntArray(pretmp)
	stater:addState(pressed,pre)
	
	local default = makeEmptyIntArray()
	stater:addState(default,norm)
	
	--seltmp = {}
	--table.insert(seltmp,R.state_focused)
	--select = makeIntArray(seltmp)
	--stater:addState(select,selectDrawable)
	

	
	return stater
end
--tabButtonStateDrawable = makeStateDrawable()
--init the first three, permanent buttons.
hideButton = luajava.newInstance("android.widget.ImageButton",context)
hideParams = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",RelativeLayoutParams.WRAP_CONTENT,uiButtonBarHeight)
hideParams:addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
hideButton:setImageDrawable(resLoader(respath,"hide.png"))
hideButton:setId(100)
--hideButton:setText("Hide")
--hideButton:setTextSize(12*density)
hideButton:setLayoutParams(hideParams)
hideButton:setOnClickListener(hider_cb)

pinButton = luajava.newInstance("android.widget.ImageButton",context)
pinParams = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",RelativeLayoutParams.WRAP_CONTENT,uiButtonBarHeight)
pinParams:addRule(RelativeLayout.LEFT_OF,100);
pinButton:setImageDrawable(resLoader(respath,"unpinned.png"))
pinButton:setId(99)
--pinButton:setText("Pin")
--pinButton:setTextSize(12*density)
pinButton:setLayoutParams(pinParams)

mainButton = luajava.newInstance("android.widget.Button",context)
mainParams = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",RelativeLayoutParams.WRAP_CONTENT,uiButtonBarHeight)
mainParams:addRule(RelativeLayout.LEFT_OF,99);
mainParams:setMargins(2,0,0,0)
mainButton:setId(98)
mainButton:setText("main")
mainButton:setTextSize(12*density)
mainButton:setLayoutParams(mainParams)
mainButtonShape = makeStateDrawable("main")
mainButton:setBackgroundDrawable(mainButtonShape)
buttonMap["main"] = mainButton
--drawableMap["main"] = mainButtonShape

resizeButton = luajava.newInstance("android.widget.ImageButton",context)
resizeParams = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",RelativeLayoutParams.WRAP_CONTENT,uiButtonBarHeight)
resizeParams:addRule(RelativeLayout.ALIGN_PARENT_LEFT)
resizeButton:setImageDrawable(resLoader(respath,"resize.png"))
resizeButton:setId(101)
--resizeButton:setText("Pull")
--resizeButton:setTextSize(12*density)
resizeButton:setLayoutParams(resizeParams)

delButton = luajava.newInstance("android.widget.ImageButton",context)
delParams = luajava.newInstance("android.widget.RelativeLayout$LayoutParams",RelativeLayoutParams.WRAP_CONTENT,uiButtonBarHeight)
delParams:addRule(RelativeLayout.RIGHT_OF,101)
delButton:setImageDrawable(resLoader(respath,"delete.png"))
delButton:setId(102)
--delButton:setText("Del")
--delButton:setTextSize(12*density)
delButton:setLayoutParams(delParams)

selectedStatetmp = {}
table.insert(selectedStatetmp,R.state_focused)
selectedState = makeIntArray(selectedStatetmp)
	
normalState = makeEmptyIntArray()

clicker = {}
function clicker.onClick(v)
	--get the associated channel to the click, this is kind of awkward for now
	--but it is the label, so we dont care.
	--sf = view:getMWidth()
	--sh = view:getMHeight()
	
	--debugPrint("testing:"..sf..":"..sh)
	label = v:getText()
	--v:setSelected(true)
	--get the "normal" drawable.
	drawable = drawableMap[label]
	
	dpaint = drawable:getPaint()
	dpaint:setARGB(225,200,200,200)
	--v:setFocused(true)
	for i,b in pairs(drawableMap) do
		if(i ~= label) then
			--debugPrint("setting "..i.." to not selected")
			--b:setSelected(false)
			--bdraw = b:getBackground()
			--bdraw:setState(normalState)
			--b:setFocused(false)
			tmp = drawableMap[i]
			tmppaint = tmp:getPaint()
			tmppaint:setARGB(225,0,0,100)
			tview = buttonMap[i]
			tview:invalidate()
		end
	end
	v:invalidate()
	PluginXCallS("updateSelection",label)
end
clicker_cb = luajava.createProxy("android.view.View$OnClickListener",clicker)
mainButton:setOnClickListener(clicker_cb)

longclicker = {}
function longclicker.onLongClick(v)
	label = v:getText()
	sendToServer(".kb popup "..label)
	return true
end
longclicker_cb = luajava.createProxy("android.view.View$OnLongClickListener",longclicker)

pinner = {}
function pinner.onClick(v)
	if(toggle == true) then
		if(pinned == true) then
			pinned = false
			pinButton:setImageDrawable(resLoader(respath,"unpinned.png"))
		else
			--if(toggle == false) then
				pinned = true
				pinButton:setImageDrawable(resLoader(respath,"pinned.png"))
			--end
		end
	end
	pinButton:invalidate()
end
pinner_cb = luajava.createProxy("android.view.View$OnClickListener",pinner)
pinButton:setOnClickListener(pinner_cb)
pinned = false

--set up the resizer button.
touchStartY = 0
windowStartHeight = 0
touchStartTime = 0
resizer = {}
function resizer.onTouch(v,e)
	
	action = e:getAction()
	if(action == MotionEvent.ACTION_DOWN) then
		debugPrint("resizer down")
		touchStartY = e:getY()
		windowStartHeight = view:getHeight()
		touchStartTime = System:currentTimeMillis()
	end
	
	if(action == MotionEvent.ACTION_MOVE) then
		now = System:currentTimeMillis()
		timeDelta = now - touchStartTime
		if(timeDelta < 30) then
			return true
		end
		touchStartTime = now
		
		delta = e:getY() - touchStartY
		if(delta ~= 0) then
			--resize window.
			twidth = view:getWidth()
			theight = view:getHeight() + delta
			wwidth = parentView:getWidth()
			wheight = parentView:getHeight() + delta
			if(wheight > maxHeight) then
				wheight = maxHeight
				theight = maxHeight - uiButtonBarHeight - 3*getDisplayDensity()
			end
			
			if(theight < minHeight) then
				theight = minHeight
				wheight = minHeight+uiButtonBarHeight
			end
			
			parentView:setDimensions(wwidth,wheight)
			
			foldoutHeight = foldoutHeight + delta
			debugPrint("resizer moving:"..delta.." calculated new height:"..theight)
			local tmpparams = luajava.new(LinearLayoutParams,twidth,theight);
			--view:setDimensions(tonumber(twidth),tonumber(theight))
			view:setLayoutParams(tmpparams)
			view:requestLayout()
			parentView:requestLayout()
			
		end
		toucheStartY = e:getY()
	end
	
	if(action == MotionEvent.ACTION_UP) then
		debugPrint("resizer up, foldout height:"..foldoutHeight.." min height"..minHeight)
		foldoutHeight = parentView:getHeight() - minHeight
		expandAnimation = luajava.newInstance("android.view.animation.TranslateAnimation",0,0,-foldoutHeight,0)
		shrinkAnimation = luajava.newInstance("android.view.animation.TranslateAnimation",0,0,0,-foldoutHeight)
		expandAnimation:setDuration(450)
		shrinkAnimation:setDuration(450)
	end
	return true
end
resizer_cb = luajava.createProxy("android.view.View$OnTouchListener",resizer)
resizeButton:setOnTouchListener(resizer_cb)

maxHeight = 0
function OnSizeChanged(neww,newh,oldw,oldh)
	--maxHeight = view:getMaxHeight()
	--debugPrint("maxHeight is: "..maxHeight)
end

PluginXCallS("initReady","now")

function OnCreate()
	--local params = parentView:getLayoutParams()
	
	debugPrint("OnCreate for CHATWINDOW"..minHeight)
	inputbar = rootView:findViewById(10)
	
	maxHeight = inputbar:getTop()
end

function setWindowSize(size)
	debugPrint("in the window set window size:"..size)
	size = tonumber(size)
	chatWindowParams = luajava.new(LinearLayoutParams,viewParams.width,size)
	minHeight = size + 3*getDisplayDensity()
	view:setLayoutParams(chatWindowParams)
	view:requestLayout()
	
end








