#version 150

in vec3 Position;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FieldKind;
uniform float FieldTime;
uniform vec2 CanvasSize;
uniform vec3 FieldScale;
uniform vec4 FieldControls;
uniform float InteriorVisibility;
uniform int Variation;
uniform vec3 PatternPhase;
uniform vec3 Palette0;
uniform vec3 Palette1;
uniform vec3 Palette2;
uniform vec3 Palette3;
uniform vec3 Palette4;
out vec4 vertexColor;

float smoothValue(float value) {
    float t = clamp(value, 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

float seed(int index, int channel) {
    uint value = uint(index) * 0x1f123bb5u + uint(channel) * 0x5f356495u+ uint(Variation) * 0x6c8e9cf5u;
    value = (value ^ (value >> 16u)) * 0x45d9f3bu;
    value = (value ^ (value >> 16u)) * 0x45d9f3bu;
    return float((value ^ (value >> 16u)) & 0x00ffffffu) / 16777216.0;
}

float fieldNoise(float x, float y) {
    int ix = int(floor(x)), iy = int(floor(y));
    float u = smoothValue(x - float(ix)), v = smoothValue(y - float(iy));
    float a = seed(ix, iy), b = seed(ix + 1, iy);
    float d = seed(ix, iy + 1), e = seed(ix + 1, iy + 1);
    return (a + (b - a) * u) * (1.0 - v) + (d + (e - d) * u) * v;
}

vec3 mixColor(vec3 a, vec3 b, float amount) {
    float t = clamp(amount, 0.0, 1.0);
    return floor(a * (1.0 - t) + b * t + 0.5);
}

float veilOpacity(vec3 color, float opacity, float rim) {
    float a = clamp(opacity, 0.0, 1.0);
    float neutral = min(color.r, min(color.g, color.b)) / 255.0;
    float fill = a * 0.70 / (1.0 + 5.0 * a * neutral);
    return fill + (a - fill) * clamp(rim, 0.0, 1.0);
}

float bell(float phase) {
    float v = sin(clamp(phase, 0.0, 1.0) * 3.141592653589793);
    return v * v;
}

vec4 opal(vec2 p) {
    float t = FieldTime * 0.22;
    float u = p.x * FieldScale.x * 0.026 * FieldScale.y;
    float v = p.y * FieldScale.x * 0.034 * FieldScale.y;
    float distortion = FieldControls.x;

    float wx = u + sin(v * 0.9 + t * 0.6) * 0.65 * distortion;
    wx += (fieldNoise(u * 0.7 + t * 0.25, v * 0.8 + 7.0) - 0.5) * 0.8 * distortion;

    float wy = v + sin(u * 0.72 - t * 0.5) * 0.55 * distortion;
    wy += (fieldNoise(u * 0.5 + 13.0, v * 0.9 + t * 0.2) - 0.5) * 0.65 * distortion;

    float cloud = fieldNoise(wx * 0.8 + 41.0 + t * 0.12, wy * 0.8 + 17.0 - t * 0.1) * 0.7+ fieldNoise(wx * 1.6 + 93.0, wy * 1.6 + 51.0 + t * 0.13) * 0.3;
    float contour = cloud - 0.52 + sin(wx * 1.1 + wy * 0.6 + t * 0.4) * 0.07;

    float vein = exp(-contour * contour * 260.0 * FieldControls.y);
    float veil = smoothValue((cloud - 0.26) / 0.42);
    float hue = 0.5 + 0.5 * sin(wx * 0.85 + wy * 0.55 - t * 0.3);

    vec3 tint = hue < 0.5 ? mixColor(Palette0, Palette1, hue * 2.0): mixColor(Palette1, Palette2, hue * 2.0 - 1.0);

    vec3 color = mixColor(tint, Palette3, veil * 0.25);
    return vec4(color, veilOpacity(color, (veil * 0.14 + vein * 0.30) * (0.75 + cloud * 0.25), vein * vein * vein));
}

vec4 prism(vec2 p) {
    float cs = cos(PatternPhase.x), sn = sin(PatternPhase.x);

    vec2 q = p * FieldScale.x * FieldScale.y;

    float u = q.x * cs - q.y * sn, v = q.x * sn + q.y * cs;
    float t = FieldTime * 0.30;

    float distortion = FieldControls.x;

    float focusWidth = min(15.0 * FieldControls.y, 18.0 / (FieldScale.y * FieldScale.y));
    float warp = sin(u * 0.045 + v * 0.075 + t + PatternPhase.y) * 0.75 * distortion;

    float field = sin(u * 0.078 + warp + t + PatternPhase.z)+ sin(v * 0.10 - t + PatternPhase.y + sin(u * 0.052 + PatternPhase.z) * 0.8 * distortion);
    float focus = exp(-field * field * focusWidth), halo = exp(-field * field * 2.8);
    float hue = 0.5 + 0.5 * sin(u * 0.026 + v * 0.041 - t * 0.4);

    vec3 color = hue < 0.5 ? mixColor(Palette0, Palette1, hue * 2.0): mixColor(Palette1, Palette2, hue * 2.0 - 1.0);

    float shimmer = 0.65 + 0.35 * fieldNoise(u * 0.05, v * 0.05 + t * 0.2);
    return vec4(color, (focus * 0.34 + halo * 0.15) * shimmer);
}

vec4 aurora(float u, float v, float pixelSize) {
    float t = FieldTime * 0.23;

    float fold = (sin(u * 8.0 + t + PatternPhase.x + sin(u * 3.0 - t * 0.4)) * 0.12+ sin(u * 17.0 - t * 0.7 + PatternPhase.y) * 0.025) * FieldControls.x;

    float distance = 0.68 + fold - v;
    float curtain = smoothValue((distance + 0.045) / 0.10)* exp(-max(0.0, distance) * 5.5 / FieldControls.z);
    float rays = 0.58 + fieldNoise(u * 38.0 + sin(v * 3.0 + t) * 0.6, t * 0.45 + 17.0) * 0.42;

    float softness = max(0.033, pixelSize * 1.5);
    float edge = exp(-distance * distance / (softness * softness));

    float second = v - (0.26 - fold * 0.55);
    float veil = exp(-second * second * 85.0) * (0.5 + 0.5 * sin(u * 6.0 - t));

    vec3 color = mixColor(Palette0, Palette1, smoothValue(distance / 0.40));
    color = mixColor(color, Palette2, edge * 0.36);

    return vec4(color, veilOpacity(color, (curtain * rays * 0.30 + edge * 0.17 + veil * 0.09)* (0.65 + bell(u) * 0.35), edge * edge));
}

vec4 fluorite(float u, float v) {
    float t = FieldTime * 0.24, distortion = FieldControls.x;

    float warpX = u * 4.2 + sin(v * 4.6 + t * 0.65 + PatternPhase.x) * 0.72 * distortion;
    float warpY = v * 4.0 + sin(u * 4.1 - t * 0.55 + PatternPhase.y) * 0.68 * distortion;

    float a = warpX + warpY * 0.55 + t * 0.65;
    float b = warpY - warpX * 0.45 - t * 0.50;

    float surface = sin(a) * 0.65 + cos(b) * 0.50;

    float dxdy = cos(v * 4.6 + t * 0.65 + PatternPhase.x) * 0.72 * 4.6 * distortion;
    float dydx = cos(u * 4.1 - t * 0.55 + PatternPhase.y) * 0.68 * 4.1 * distortion;

    float du = cos(a) * 0.65 * (4.2 + dydx * 0.55) - sin(b) * 0.50 * (dydx - 4.2 * 0.45);
    float dv = cos(a) * 0.65 * (dxdy + 4.0 * 0.55) - sin(b) * 0.50 * (4.0 - dxdy * 0.45);

    float normal = sqrt(du * du + dv * dv + 6.25);
    float reflection = clamp((du * -0.38 + dv * -0.28 + 2.5 * 0.88) / normal, 0.0, 1.0);

    float shine = pow(reflection, 5.0 * FieldControls.y);
    float sheen = smoothValue((reflection - 0.28) / 0.65);

    float hue = 0.5 + 0.5 * sin(surface * 2.6 + u * 1.3 - v * 0.8 - t * 0.30);

    vec3 color = hue < 0.5 ? mixColor(Palette0, Palette1, hue * 2.0): mixColor(Palette1, Palette2, hue * 2.0 - 1.0);
    color = mixColor(color, Palette3, shine * 0.62);

    float pools = smoothValue((surface + 0.55) / 1.4);

    return vec4(color, veilOpacity(color, 0.055 + pools * 0.075 + sheen * 0.15 + shine * 0.30, shine * shine * shine));
}

float focus(float distance, float width) {
    float d = distance / width;
    return exp(-d * d);
}

vec4 astral(float u, float v, float pixelSize) {
    float t = FieldTime * 0.28;

    float fold = v - 0.52 + (sin(u * 3.8 + t * 0.75 + PatternPhase.x) * 0.30+ sin(u * 7.2 - t * 0.55) * 0.09) * FieldControls.x;

    float crossing = u - 0.50 + (sin(v * 3.6 - t * 0.55 + 2.0 + PatternPhase.y) * 0.32+ sin(v * 6.0 + t * 0.45) * 0.07) * FieldControls.x;
    float softness = max(0.025 / FieldControls.y, pixelSize * 1.7);

    float blueHem = focus(fold, softness), goldHem = focus(crossing, softness * 1.3);

    float blueVeil = focus(fold, fold < 0.0 ? 0.18 : 0.055);
    float goldVeil = focus(crossing, crossing > 0.0 ? 0.22 : 0.07);

    float blue = (blueVeil * 0.23 + blueHem * 0.27) * (0.65 + bell(u) * 0.35);
    float gold = (goldVeil * 0.18 + goldHem * 0.20) * (0.55 + bell(v) * 0.45);

    vec3 cool = mixColor(Palette0, Palette1, 0.5 + 0.5 * sin(u * 3.0 - t * 0.4));
    vec3 warm = mixColor(Palette3, Palette2, 0.5 + 0.5 * sin(v * 3.0 + t * 0.3));

    vec3 color = mixColor(cool, warm, gold / max(0.001, blue + gold));
    color = mixColor(color, Palette4, blueHem * goldHem * 0.35);

    float rim = max(blueHem, goldHem);

    return vec4(color, veilOpacity(color, blue + gold, rim * rim));
}

void main() {
    vec2 p = Position.xy;

    vec4 sampleColor;

    if (FieldKind == 0) {
        sampleColor = opal(p);
    }
    else if (FieldKind == 1) {
        sampleColor = prism(p);
    }
    else {
        vec2 uv = 0.5 + (p - CanvasSize * 0.5) / 144.0 * FieldScale.y;
        float pixelSize = FieldScale.z * FieldScale.y;
        if (FieldKind == 2) {
            sampleColor = aurora(uv.x, uv.y, pixelSize);
        }
        else if (FieldKind == 3) {
            sampleColor = fluorite(uv.x, uv.y);
        }
        else {
            sampleColor = astral(uv.x, uv.y, pixelSize);
        }

        sampleColor.a = floor(sampleColor.a * 255.0 + 0.5) / 255.0;
    }

    float inset = min(min(p.x, CanvasSize.x - p.x), min(p.y, CanvasSize.y - p.y));

    float visibility = (1.0 - (1.0 - InteriorVisibility) * smoothValue(inset / 10.0))* smoothValue((inset + 28.0) / 12.0);

    float alpha = floor(255.0 * clamp(sampleColor.a, 0.0, 1.0) * visibility * FieldControls.w + 0.5);

    vertexColor = vec4(sampleColor.rgb, alpha) / 255.0;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
