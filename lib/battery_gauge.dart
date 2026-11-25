import 'dart:math';
import 'package:flutter/material.dart';

class BatteryGauge extends StatelessWidget {
  final int niveau;
  final bool enCharge;
  const BatteryGauge({super.key, required this.niveau, required this.enCharge});

  static Color couleur(double v) => v > 0.5
      ? const Color(0xFF4ADE80)
      : v > 0.2
          ? const Color(0xFFFBBF24)
          : const Color(0xFFF87171);

  @override
  Widget build(BuildContext context) {
    return TweenAnimationBuilder<double>(
      tween: Tween(end: niveau / 100),
      duration: const Duration(milliseconds: 700),
      curve: Curves.easeOutCubic,
      builder: (_, v, __) {
        final c = couleur(v);
        return SizedBox(
          width: 200, height: 200,
          child: CustomPaint(
            painter: _GaugePainter(v, c),
            child: Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text('${(v * 100).round()}%',
                      style: const TextStyle(fontSize: 46, fontWeight: FontWeight.w700)),
                  if (enCharge)
                    Row(mainAxisSize: MainAxisSize.min, children: [
                      Icon(Icons.bolt_rounded, color: c, size: 20),
                      Text('En charge', style: TextStyle(color: c)),
                    ]),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}

class _GaugePainter extends CustomPainter {
  final double v;
  final Color c;
  _GaugePainter(this.v, this.c);

  @override
  void paint(Canvas canvas, Size size) {
    final rect = Rect.fromCircle(
        center: size.center(Offset.zero), radius: size.width / 2 - 14);
    final base = Paint()
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round
      ..strokeWidth = 14
      ..color = Colors.white.withOpacity(0.08);
    canvas.drawArc(rect, 0, 2 * pi, false, base);
    final lueur = Paint()
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round
      ..strokeWidth = 20
      ..color = c.withOpacity(0.35)
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 8);
    final arc = Paint()
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round
      ..strokeWidth = 14
      ..color = c;
    canvas.drawArc(rect, -pi / 2, 2 * pi * v, false, lueur);
    canvas.drawArc(rect, -pi / 2, 2 * pi * v, false, arc);
  }

  @override
  bool shouldRepaint(covariant _GaugePainter o) => o.v != v || o.c != c;
}
