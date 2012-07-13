##author: Dylan Mead, University of Oregon
##purpose: to translate pixel addresses for Hama 2 wafer
##input: known pixel number, address scheme for input pixel number
##output: corresponding address or cartesian coordinate (origin at center)
##usage: python pixel_translate.py (known/input pixel, int from [1,1024]) (address scheme of input pixel, h or k (see below)) (desired output format, h or k or c (see below))
##data containing file "pixellist" must be in same directory as "pixel_translate.py"

#program will shutdown if input is not entered correctly


import sys

#inpix has value 1 to 1024 (interpreted as a string)
inpix = sys.argv[1]

### "h" coresponds to hama scheme
### "k" coresponds to kpix gui scheme
### "c" coresponds to cartesian coordinates

#inscheme has value "h" or "k"
inscheme = sys.argv[2]

#outform has value "h" or "k" or "c"
outform = sys.argv[3]

#pulls data from comma delimited file. form: hama,kpix,x,y
phile=open('pixellist.csv')
strng = phile.read()
lits = strng.split('\r\n')

#set up list and dummies
templst = []
tempstr=''

if inscheme == 'h':
	if outform == 'h':
		print inpix
		sys.exit()
	elif outform == 'k':
		for i in range(1024):
			tempstr = lits[i]
			templst = tempstr.split(',')
			if templst[0] == inpix:
				print templst[1]
				sys.exit()
	elif outform == 'c':
		for i in range(1024):
			tempstr = lits[i]
			templst = tempstr.split(',')
			if templst[0] == inpix:
				print templst[2]
				print templst[3]
				sys.exit()
	else:
		sys.exit()
elif inscheme == 'k':
	if outform == 'k':
		print inpix
		sys.exit()
	elif outform == 'h':
		for i in range(1024):
			tempstr = lits[i]
			templst = tempstr.split(',')
			if templst[1] == inpix:
				print templst[0]
				sys.exit()
	elif outform == 'c':
		for i in range(1024):
			tempstr = lits[i]
			templst = tempstr.split(',')
			if templst[1] == inpix:
				print templst[2]
				print templst[3]
				sys.exit()
	else:
		sys.exit()
else:
	sys.exit()

