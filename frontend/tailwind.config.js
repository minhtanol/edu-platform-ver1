export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        border: '#26354d',
        background: '#070b14',
        foreground: '#e6edf7',
        surface: '#0e1626',
        panel: '#111c2e',
        muted: '#93a4b8',
        primary: '#22d3ee',
        accent: '#a3e635',
        danger: '#fb7185'
      },
      boxShadow: {
        soft: '0 1px 1px rgba(0, 0, 0, 0.25), 0 24px 70px rgba(0, 0, 0, 0.34)'
      }
    }
  },
  plugins: []
};
