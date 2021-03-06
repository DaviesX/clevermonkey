t0 = 0;
t1 = 1;

p0 = [0, 0];
p1 = [0.5, 3.0];
p2 = [0.8, 3.0];
p3 = [1.0, 1.0];

t = [t0 : (t1 - t0)/100 : t1];

fx = (1 - t).^3.*p0(1) + (1 - t).^2.*t.*p1(1) + (1 - t).*t.^2.*p2(1) + t.^3.*p3(1);
fy = (1 - t).^3.*p0(2) + (1 - t).^2.*t.*p1(2) + (1 - t).*t.^2.*p2(2) + t.^3.*p3(2);

plot(fx, fy, "r");