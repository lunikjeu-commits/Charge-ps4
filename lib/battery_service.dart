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

class Reglages {
  int seuil1;
  int seuil2;
  bool auto;
  Reglages(this.seuil1, this.seuil2, this.auto);
}

class Historique {
  final List<List<int>> points; // [temps_ms, niveau]
  final int? reste; // minutes
  const Historique(this.points, this.reste);
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

  static Future<void> demarrerSuivi() async {
    try {
      await _canal.invokeMethod('startService');
    } catch (_) {}
  }

  static Future<void> arreterSuivi() async {
    try {
      await _canal.invokeMethod('stopService');
    } catch (_) {}
  }

  static Future<Reglages> lireReglages() async {
    try {
      final m = Map<String, dynamic>.from(await _canal.invokeMethod('getReglages') as Map);
      return Reglages(m['seuil1'] as int, m['seuil2'] as int, m['auto'] as bool);
    } catch (_) {
      return Reglages(20, 10, true);
    }
  }

  static Future<void> ecrireReglages(Reglages r) async {
    try {
      await _canal.invokeMethod(
          'setReglages', {'seuil1': r.seuil1, 'seuil2': r.seuil2, 'auto': r.auto});
    } catch (_) {}
  }

  static Future<Historique> lireHistorique() async {
    try {
      final m = Map<String, dynamic>.from(await _canal.invokeMethod('getHistorique') as Map);
      final pts = (m['points'] as List)
          .map((e) => (e as List).map((x) => (x as num).toInt()).toList())
          .toList();
      return Historique(pts, m['reste'] as int?);
    } catch (_) {
      return const Historique([], null);
    }
  }
}
