import json
import os
import re
import glob
import pandas as pd
import matplotlib.pyplot as plt

RESULTS_DIR = 'results'
OUT_DIR = 'results/plots'
os.makedirs(OUT_DIR, exist_ok=True)

FILENAME_RE = re.compile(
    r'(?P<origin>local|server|hlv)_cpu(?P<cpu>\d+(?:\.\d+)?)_w(?P<write>\d+)_r(?P<read>\d+)\.json'
)

def parse_meta(path):
    m = FILENAME_RE.match(os.path.basename(path))
    if not m: return None
    d = m.groupdict()
    d['cpu'] = float(d['cpu'])
    d['profile'] = f"{d['write']}/{d['read']}"
    d['origin'] = 'local->server' if d['origin'] == 'local' else 'server->server'
    return d

def load_k6_json(path, meta):
    rows = []
    with open(path, encoding='utf-8') as f:
        for line in f:
            try:
                d = json.loads(line)
            except json.JSONDecodeError:
                continue

            if d.get('type') != 'Point' or d.get('metric') != 'http_req_duration':
                continue

            data = d.get('data', {})
            val = data.get('value')
            if val is None: continue

            # Оставляем только нужные поля для агрегации
            rows.append({
                'value': val,
                'operation': data.get('tags', {}).get('operation'),
                'expected_response': str(data.get('tags', {}).get('expected_response', '')).lower(),
                **meta
            })
    return pd.DataFrame(rows)

def load_all():
    frames = []
    for path in glob.glob(os.path.join(RESULTS_DIR, '*.json')):
        meta = parse_meta(path)
        if meta:
            df = load_k6_json(path, meta)
            if not df.empty:
                frames.append(df)
    return pd.concat(frames, ignore_index=True) if frames else pd.DataFrame()

def aggregate(df):
    # Оставляем только успешные запросы и считаем среднее
    return (
        df[df['expected_response'] == 'true']
        .groupby(['origin', 'profile', 'cpu', 'operation'])['value']
        .mean()
        .reset_index(name='avg')
        .sort_values(['origin', 'profile', 'operation', 'cpu'])
    )

def plot_avg(agg):
    profiles = ['5/95', '50/50', '95/5']
    origins = ['local->server', 'server->server']
    ops = [('create', 'POST /visitors/'), ('read', 'GET /exhibits/rating')]

    for origin in origins:
        fig, axes = plt.subplots(1, 3, figsize=(18, 5), sharey=True)
        fig.suptitle(f'Avg response time vs CPU cores ({origin})', fontsize=14)

        for ax, profile in zip(axes, profiles):
            sub = agg[(agg['origin'] == origin) & (agg['profile'] == profile)]
            for op, label in ops:
                op_df = sub[sub['operation'] == op]
                if op_df.empty: continue

                ax.plot(op_df['cpu'], op_df['avg'], marker='o', linewidth=2, label=label)
                for _, r in op_df.iterrows():
                    ax.annotate(f"{r['avg']:.1f}", (r['cpu'], r['avg']),
                                textcoords='offset points', xytext=(0, 8),
                                ha='center', fontsize=8)
            ax.set_title(f'write/read = {profile}')
            ax.set_xlabel('CPU cores')
            ax.grid(True, linestyle='--', alpha=0.4)

            if not sub.empty:
                ax.set_xticks(sorted(sub['cpu'].unique()))

        axes[0].set_ylabel('Avg response time (ms)')
        handles, labels = axes[0].get_legend_handles_labels()
        if handles:
            fig.legend(handles, labels, loc='upper center', ncol=2)

        fig.tight_layout(rect=[0, 0, 1, 0.92])
        fig.savefig(os.path.join(OUT_DIR, f'{origin.replace("->","_")}_avg.png'), dpi=150)
        plt.close(fig)

def main():
    df = load_all()
    if df.empty:
        print('Нет данных')
        return

    agg = aggregate(df)
    agg.to_csv(os.path.join(OUT_DIR, 'cpu_scaling_summary.csv'), index=False)
    plot_avg(agg)
    print('Готово')

if __name__ == '__main__':
    main()