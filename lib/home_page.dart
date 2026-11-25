import 'dart:async';
import 'package:flutter/material.dart';
import 'battery_gauge.dart';
import 'battery_service.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  List<Manette> _liste = [];
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _actualiser();
    _timer = Timer.periodic(const Duration(seconds: 5), (_) => _actualiser());
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _actualiser() async {
    final l = await BatteryService.lire();
    if (mounted) setState(() => _liste = l);
  }

  void _apropos() {
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('À propos'),
        content: const Text('Batterie Manette v1.0\nCréé par lunikapp\nLC STUDIO'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('OK'))
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFF0B1426), Color(0xFF142240)],
          ),
        ),
        child: SafeArea(
          child: Column(children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(24, 16, 8, 0),
              child: Row(children: [
                const Expanded(
                  child: Text('Batterie manette',
                      style: TextStyle(fontSize: 24, fontWeight: FontWeight.w700)),
                ),
                IconButton(onPressed: _apropos, icon: const Icon(Icons.info_outline)),
              ]),
            ),
            Expanded(
              child: RefreshIndicator(
                onRefresh: _actualiser,
                child: ListView(
                  physics: const AlwaysScrollableScrollPhysics(),
                  padding: const EdgeInsets.all(20),
                  children: _liste.isEmpty
                      ? [_vide()]
                      : _liste.map(_carte).toList(),
                ),
              ),
            ),
          ]),
        ),
      ),
    );
  }

  Widget _vide() => Padding(
        padding: const EdgeInsets.only(top: 80),
        child: Column(children: [
          Icon(Icons.sports_esports_rounded, size: 80, color: Colors.white.withOpacity(0.3)),
          const SizedBox(height: 16),
          const Text('Aucune manette détectée',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          Text('Allume la manette (touche PS) et connecte-la en Bluetooth.',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.white.withOpacity(0.6))),
        ]),
      );

  Widget _carte(Manette m) {
    final ok = m.supporte && m.niveau >= 0;
    return Card(
      color: Colors.white.withOpacity(0.06),
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
      margin: const EdgeInsets.only(bottom: 16),
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(children: [
          Text(m.nom, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w600)),
          const SizedBox(height: 20),
          if (ok)
            BatteryGauge(niveau: m.niveau, enCharge: m.enCharge)
          else
            Text(
              m.supporte
                  ? 'Niveau momentanément indisponible.'
                  : 'Ce téléphone ne fournit pas le niveau de cette manette (Android 12 minimum requis).',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.white.withOpacity(0.7)),
            ),
          if (ok && m.pleine)
            const Padding(
              padding: EdgeInsets.only(top: 12),
              child: Text('Batterie pleine'),
            ),
        ]),
      ),
    );
  }
}
