import 'package:flutter/material.dart';

class SplashScreen extends StatefulWidget {
  final Widget next;
  const SplashScreen({super.key, required this.next});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen>
    with SingleTickerProviderStateMixin {
  static const _titre = 'LC STUDIO';
  late final AnimationController _c;

  @override
  void initState() {
    super.initState();
    // 0,5 s logo + 0,8 s texte + 0,5 s pause = 1,8 s
    _c = AnimationController(vsync: this, duration: const Duration(milliseconds: 1800))
      ..forward().whenComplete(_suite);
  }

  void _suite() {
    if (!mounted) return;
    Navigator.of(context).pushReplacement(PageRouteBuilder(
      transitionDuration: const Duration(milliseconds: 400),
      pageBuilder: (_, __, ___) => widget.next,
      transitionsBuilder: (_, a, __, child) => FadeTransition(opacity: a, child: child),
    ));
  }

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final logo = CurvedAnimation(parent: _c, curve: const Interval(0, 0.278, curve: Curves.easeOutCubic));
    final texte = CurvedAnimation(parent: _c, curve: const Interval(0.278, 0.722));
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFF0B1426), Color(0xFF142240)],
          ),
        ),
        child: Center(
          child: AnimatedBuilder(
            animation: _c,
            builder: (_, __) {
              final n = (texte.value * _titre.length).ceil().clamp(0, _titre.length);
              return Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Opacity(
                    opacity: logo.value,
                    child: Transform.scale(
                      scale: 0.8 + 0.2 * logo.value,
                      child: const SizedBox(
                        width: 120, height: 120,
                        child: CustomPaint(painter: _LogoPainter()),
                      ),
                    ),
                  ),
                  const SizedBox(height: 28),
                  Text.rich(
                    TextSpan(children: [
                      TextSpan(text: _titre.substring(0, n),
                          style: const TextStyle(color: Colors.white)),
                      TextSpan(text: _titre.substring(n),
                          style: const TextStyle(color: Colors.transparent)),
                    ]),
                    style: const TextStyle(
                        fontSize: 22, fontWeight: FontWeight.w600, letterSpacing: 8),
                  ),
                ],
              );
            },
          ),
        ),
      ),
    );
  }
}

class _LogoPainter extends CustomPainter {
  const _LogoPainter();

  @override
  void paint(Canvas canvas, Size size) {
    canvas.scale(size.width / 100, size.height / 100);
    const grad = LinearGradient(colors: [Color(0xFF7CC4FF), Color(0xFF4FE3C1)]);
    final shader = grad.createShader(const Rect.fromLTWH(0, 0, 100, 100));
    final cadre = Paint()
      ..shader = shader
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3.5;
    canvas.drawRRect(
        RRect.fromRectAndRadius(const Rect.fromLTWH(6, 6, 88, 88), const Radius.circular(28)),
        cadre);
    final trait = Paint()
      ..shader = shader
      ..style = PaintingStyle.stroke
      ..strokeWidth = 7
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round;
    // L
    canvas.drawPath(
        Path()..moveTo(32, 26)..lineTo(32, 74)..lineTo(56, 74), trait);
    // C imbriqué, ouvert à droite
    canvas.drawArc(Rect.fromCircle(center: const Offset(58, 48), radius: 20),
        0.75, 6.283 - 1.5, false, trait);
  }

  @override
  bool shouldRepaint(covariant CustomPainter old) => false;
}
