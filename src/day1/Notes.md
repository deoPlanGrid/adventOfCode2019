# Advent of Code 2019: Day 1

This is actually my first time doing advent of code. I think I vaguely recall hearing about it
before this year but I've actually jumped in this year and worked on (some) of the problems.

One of the reasons [Eric Wastl](http://was.tl/) mentions on his website for _why_ people
work on AoC is that they are trying to learn a new programming language. It has been a few years
since I had done anything with Clojure and this seemed like a good opportunity. A chance
to relearn some fundamentals and see how it (and possibly myself) had changed in the meantime.

## The Problem

The problem for Day 1 was actually somewhat straightforward. Part 1:

> Fuel required to launch a given module is based on its mass. Specifically, to find the 
> fuel required for a module, take its mass, divide by three, round down, and subtract 2.
> For example:
> - For a mass of 12, divide by 3 and round down to get 4, then subtract 2 to get 2.
> - For a mass of 14, dividing by 3 and rounding down still yields 4, so the fuel required is also 2.
> - For a mass of 1969, the fuel required is 654.
> - For a mass of 100756, the fuel required is 33583.
> The Fuel Counter-Upper needs to know the total fuel requirement. To find it, individually 
> calculate the fuel needed for the mass of each module (your puzzle input), then add together 
> all the fuel values.

Not too bad right? You might run into problems with large integers but this didn't actually happen
so no worries there. But then, as is perhaps traditional with AoC, the twist came in part 2:

> Fuel itself requires fuel just like a module - take its mass, divide by three, round down, 
> and subtract 2. However, that fuel also requires fuel, and that fuel requires fuel, and so on. 
> Any mass that would require negative fuel should instead be treated as if it requires zero fuel;
> the remaining mass, if any, is instead handled by wishing really hard, which has no mass and 
> is outside the scope of this calculation.
> So, for each module mass, calculate its fuel and add it to the total. Then, treat the fuel 
> amount you just calculated as the input mass and repeat the process, continuing until a fuel 
> requirement is zero or negative. For example:
> - A module of mass 14 requires 2 fuel. This fuel requires no further fuel (2 divided by 3 
> and rounded down is 0, which would call for a negative fuel), so the total fuel required is 
> still just 2.
> - At first, a module of mass 1969 requires 654 fuel. Then, this fuel requires 216 more fuel 
> (654 / 3 - 2). 216 then requires 70 more fuel, which requires 21 fuel, which requires 5 fuel, 
> which requires no further fuel. So, the total fuel required for a module of mass 1969 is 
> 654 + 216 + 70 + 21 + 5 = 966.
> - The fuel required by a module of mass 100756 and its fuel is: 
> 33583 + 11192 + 3728 + 1240 + 411 + 135 + 43 + 12 + 2 = 50346.

The problem just naturally screams for a recursive solution (fuel for fuel...) that I couldn't
help myself in this case. Plus, Clojure by design pushes you in the direction of recursion as
opposed to a more imperative style while/for loop with an accumulator.

## Working with Clojure

Like I said, it's been a few years since I've written anything in Clojure. My day job involves 
mainly Python with a smattering of other languages as necessary (looking at you shell....). Python
has been quite nice to work with over the years, with the dynamic typing making prototypes easy,
the familiar syntax, and ease of interacting with the Internet, APIs, and the world in general. All
that said, I was pleasantly surprised to see how much Clojure had developed in the interim. Running
"scripts" in Clojure has become a breeze! All you need is the `-main` function and a `deps.edn` file
and you're good to go. This was a far cry from a couple years ago when you couldn't do things like 
this and scripting in Clojure was a PITA. Even with the "horrendously long startup times", my 
scripts didn't take all that long to run. Honestly I'm not sure why people are moaning about startup
times with scripts. I get the impression that as a project grows, however, that startup time
becomes an issue. For now, I'm not too worried.

Another wonderful feature I had forgotten was the centralized dependency management you get with
Clojure. Regardless of what build tool you use for Clojure, be it `clj`, `leiningen`, `boot`, or
even Gradle, you specify dependencies, aliases, build parameters, and all other administrative
stuff in *one* file. This style of package/script management reminded me that Python still has
some way to go in this area. Rust and JVM languages certainly got it right when they centralized
package specification files. `poetry` has certainly simplified this process for Python and I'm
hopeful that it continues to set the standard for easy package management.
