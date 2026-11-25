import 'package:flutter/material.dart';

class HistoriqueChart extends StatelessWidget {
  final List<List<int>> points; // [temps_ms, niveau]
  final Color couleur;
  const HistoriqueChart({super.key, required this.points, required this.couleur});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 90,
      width: double.infinity,
      child: CustomPaint(painter: _ChartPainter(points, couleur)),
    );
  }
}

class _ChartPainter extends CustomPainter {
  final List<List<int>> pts;
  final Color c;
  _ChartPainter(this.pts, this.c);

  @override
  void paint(Canvas canvas, Size s) {
    final grille = Paint()
      ..color = Colors.white.withOpacity(0.08)
      ..strokeWidth = 1;
    canvas.drawLine(Offset(0, s.height / 2), Offset(s.width, s.height / 2), grille);
    if (pts.length < 2) return;
    final t0 = pts.first[0];
    final dt = (pts.last[0] - t0).toDouble();
    if (dt <= 0) return;
    double x(int i) => (pts[i][0] - t0) / dt * s.width;
    double y(int i) => s.height - pts[i][1] / 100 * s.height;

    final ligne = Path()..moveTo(x(0), y(0));
    for (var i = 1; i < pts.length; i++) {
      ligne.lineTo(x(i), y(i - 1));
      ligne.lineTo(x(i), y(i));
    }
    final zone = Path.from(ligne)
      ..lineTo(s.width, s.height)
      ..lineTo(0, s.height)
      ..close();
    canvas.drawPath(
        zone,
        Paint()
          ..shader = LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [c.withOpacity(0.35), c.withOpacity(0)],
          ).createShader(Offset.zero & s));
    canvas.drawPath(
        ligne,
        Paint()
          ..color = c
          ..style = PaintingStyle.stroke
          ..strokeWidth = 3
          ..strokeJoin = StrokeJoin.round
          ..strokeCap = StrokeCap.round);
  }

  @override
  bool shouldRepaint(covariant _ChartPainter o) => o.pts.length != pts.length || o.c != c;
}
