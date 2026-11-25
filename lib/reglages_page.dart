import 'package:flutter/material.dart';
import 'battery_service.dart';

class ReglagesPage extends StatefulWidget {
  const ReglagesPage({super.key});

  @override
  State<ReglagesPage> createState() => _ReglagesPageState();
}

class _ReglagesPageState extends State<ReglagesPage> {
  Reglages? _r;

  @override
  void initState() {
    super.initState();
    BatteryService.lireReglages().then((r) {
      if (mounted) setState(() => _r = r);
    });
  }

  Future<void> _auto(bool v) async {
    setState(() => _r!.auto = v);
    await BatteryService.ecrireReglages(_r!);
    if (v) {
      await BatteryService.demarrerSuivi();
    } else {
      await BatteryService.arreterSuivi();
    }
  }

  @override
  Widget build(BuildContext context) {
    final r = _r;
    return Scaffold(
      backgroundColor: const Color(0xFF0B1426),
      appBar: AppBar(title: const Text('Réglages'), backgroundColor: Colors.transparent),
      body: r == null
          ? const Center(child: CircularProgressIndicator())
          : ListView(padding: const EdgeInsets.all(12), children: [
              SwitchListTile(
                value: r.auto,
                onChanged: _auto,
                title: const Text('Suivi automatique'),
                subtitle: const Text(
                    'Démarre avec le téléphone. L\'icône et le % apparaissent dès que la manette se connecte.'),
              ),
              const SizedBox(height: 12),
              ListTile(
                title: Text('Alerte batterie faible : ${r.seuil1} %'),
                subtitle: Slider(
                  min: 15, max: 50, divisions: 7,
                  value: r.seuil1.toDouble(),
                  onChanged: (v) => setState(() => r.seuil1 = v.round()),
                  onChangeEnd: (_) => BatteryService.ecrireReglages(r),
                ),
              ),
              ListTile(
                title: Text('Alerte critique : ${r.seuil2} %'),
                subtitle: Slider(
                  min: 5, max: 14, divisions: 9,
                  value: r.seuil2.toDouble(),
                  onChanged: (v) => setState(() => r.seuil2 = v.round()),
                  onChangeEnd: (_) => BatteryService.ecrireReglages(r),
                ),
              ),
            ]),
    );
  }
}
