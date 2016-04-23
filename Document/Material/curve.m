the = pi/100;
l = 2;
v = 3;

t0 = 0;
t1 = 5;

t = [t0 : (t1 - t0)/1000 : t1];

w = v/l*sin(the);

fx = l*cot(the) - l/sin(the)*cos(w*t + the);
fy = -l + l/sin(the)*sin(w*t + the);

gx = l*cot(the) - l*cot(the)*cos(w*t);
gy = l*cot(the)*sin(w*t);

plot(fx, fy, "r");
hold on;
plot(gx, gy, "g");
