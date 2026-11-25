import 'package:flutter/services.dart';

class Manette {
  final String nom;
  final int niveau; // -1 = inconnu
  final int statut; // 1 = en charge, 4 = pleine
  final bool supporte;

  const Manette(this.nom, this.niveau, this.statut, this.supporte);

  bool get enCharge => statut == 1;
  bool get pleine => statut == 4;
}

class BatteryService {
  static const _canal = MethodChannel('lc/manette');

  static Future<List<Manette>> lire() async {
    try {
      final r = await _canal.invokeMethod<List<dynamic>>('getControllers') ?? [];
      return r.map((e) {
        final m = Map<String, dynamic>.from(e as Map);
        return Manette(
          m['name'] as String? ?? 'Manette',
          m['level'] as int? ?? -1,
          m['status'] as int? ?? 0,
          m['supported'] as bool? ?? false,
        );
      }).toList();
    } catch (_) {
      return [];
    }
  }
}
