for i=1,5 do
	print("Hallo!!!")
end

i = 10.5
print(i)

function myprint ()
	print(11)
end

myprint()

function boundPrint ()
	bound = luajava.newInstance( "de.d2dev.fourseasons.prototypes.lua.BindMe" )
	bound:printOnConsole()
end

boundPrint()
boundPrint()




