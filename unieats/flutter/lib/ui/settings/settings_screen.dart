import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../providers/providers.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  Future<void> _fetchFlags(BuildContext context, WidgetRef ref) async {
    final messenger = ScaffoldMessenger.of(context);
    try {
      await ref.read(remoteConfigProvider.notifier).fetchAndActivate();
      messenger.showSnackBar(const SnackBar(content: Text('Flags activated')));
    } on Exception catch (e) {
      messenger.showSnackBar(SnackBar(content: Text('Fetch failed: $e')));
    }
  }

  Future<void> _testCrash(BuildContext context, WidgetRef ref) async {
    final messenger = ScaffoldMessenger.of(context);
    final consent = ref.read(crashConsentProvider).valueOrNull ?? false;
    await ref
        .read(crashReporterProvider)
        .recordError(
          StateError('Test crash from Settings'),
          StackTrace.current,
        );
    messenger.showSnackBar(
      SnackBar(
        content: Text(
          consent
              ? 'Test crash recorded (see the "crash" log)'
              : 'Not sent: crash reporting is off',
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(authProvider).valueOrNull;
    final flags = ref.watch(remoteConfigProvider);
    final consent = ref.watch(crashConsentProvider);
    final fetchedAt = flags.fetchedAt;

    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        children: [
          const _Header('Remote config'),
          ListTile(
            title: const Text('show_new_rating_ui'),
            subtitle: Text(
              fetchedAt == null
                  ? 'Default (not fetched yet)'
                  : 'Fetched at ${TimeOfDay.fromDateTime(fetchedAt).format(context)}',
            ),
            trailing: Text(flags.showNewRatingUi ? 'true' : 'false'),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: FilledButton.tonal(
              onPressed: () => _fetchFlags(context, ref),
              child: const Text('Fetch & activate'),
            ),
          ),
          const _Header('Crash reporting'),
          SwitchListTile(
            title: const Text('Share crash reports'),
            subtitle: const Text('Off until you opt in'),
            value: consent.valueOrNull ?? false,
            onChanged:
                consent.hasValue
                    ? (v) => ref.read(crashConsentProvider.notifier).set(v)
                    : null,
          ),
          if (kDebugMode)
            ListTile(
              leading: const Icon(Icons.bug_report_outlined),
              title: const Text('Test crash'),
              subtitle: const Text('Debug builds only'),
              onTap: () => _testCrash(context, ref),
            ),
          const _Header('Account'),
          ListTile(
            leading: const Icon(Icons.person_outline),
            title: Text(user?.displayName ?? ''),
            subtitle: Text(user?.email ?? ''),
          ),
          ListTile(
            leading: const Icon(Icons.logout),
            title: const Text('Sign out'),
            onTap: () => ref.read(authProvider.notifier).logout(),
          ),
        ],
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header(this.title);

  final String title;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.fromLTRB(16, 24, 16, 8),
    child: Text(
      title,
      style: Theme.of(context).textTheme.titleSmall?.copyWith(
        color: Theme.of(context).colorScheme.primary,
      ),
    ),
  );
}
