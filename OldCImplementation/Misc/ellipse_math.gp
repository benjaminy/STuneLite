set term x11

d3 = 1.0
perimeter( d1, d2 ) = d1 + d2 + d3
halfperi( d1, d2 ) = perimeter( d1, d2 ) / 2.0
area( d1, d2 ) = sqrt( halfperi( d1, d2 ) * ( halfperi( d1, d2 ) - d1 ) * ( halfperi( d1, d2 ) - d2 ) * ( halfperi( d1, d2 ) - 1.0 ) )
height( d1, d2 ) = 2.0 * area( d1, d2 ) / 1.0
base1( d1, d2 ) = sqrt( d1 * d1 - height( d1, d2 ) ** 2.0 )
ellipsex( d1, d2 ) = 0.5 - base1( d1, d2 )
alpharaw( d1, d2 ) = height( d1, d2 ) / sqrt( 0.25 - ( ellipsex( d1, d2 ) ** 2.0 ) )
alphamask( d1, d2 ) = ( d1 + d2 < 1.0 ) ? (1/0) : ( ( d1 ** 2.0 > d2 ** 2.0 + 1 ) ? (1/0) : ( ( d2 ** 2.0 > d1 ** 2.0 + 1 ) ? (1/0) : alpharaw( d1, d2 ) ) )
distscale( d1, d2 ) = 1.0 + ( 1.0 / ( 1.0 + alphamask( d1, d2 ) ) )

set isosamples 50
set hidden3d trianglepattern 7

set contour both
set cntrparam bspline
set cntrparam points 10
set cntrparam levels auto 100

splot [0:3][0:3][0:10] alphamask( x, y )

set cntrparam levels auto 10

pause -1

splot [0:3][0:3][1:2] distscale( x, y )

pause -1
