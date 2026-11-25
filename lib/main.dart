import 'package:flutter/material.dart';
import 'splash.dart';
import 'home_page.dart';

void main() => runApp(const LcApp());

class LcApp extends StatelessWidget {
  const LcApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Batterie Manette',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        brightness: Brightness.dark,
        useMaterial3: true,
        colorSchemeSeed: const Color(0xFF4F8CFF),
      ),
      home: const SplashScreen(next: HomePage()),
    );
  }
}
