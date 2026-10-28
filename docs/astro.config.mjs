// @ts-check
import { readFileSync } from 'node:fs';
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

const version = readFileSync(new URL('./version.txt', import.meta.url), 'utf8').trim();

/** Replaces `%latest_version%` with the content of `version.txt`. */
function latestVersion() {
	/** @param {any} node */
	const visit = (node) => {
		if (typeof node.value === 'string') {
			node.value = node.value.replaceAll('%latest_version%', version);
		}
		node.children?.forEach(visit);
	};
	return visit;
}

export default defineConfig({
	site: 'https://martinbonnin.github.io',
	base: '/galak',
	markdown: {
		remarkPlugins: [latestVersion],
	},
	integrations: [
		starlight({
			title: 'Galak',
			logo: {
				src: './src/assets/logo.svg',
			},
			favicon: '/favicon-96x96.png',
			editLink: {
				baseUrl: 'https://github.com/martinbonnin/galak/edit/main/docs/',
			},
			social: {
				github: 'https://github.com/martinbonnin/galak',
			},
			sidebar: [
				{
					label: 'Welcome',
					items: [
						{ label: 'Getting started', link: '/' },
						{ label: 'Generating a schema', link: '/schema' },
						{ label: 'Custom scalars', link: '/scalars' },
						{ label: 'Execution context', link: '/execution-context' },
						{ label: 'Persisted documents', link: '/persisted-documents' },
						{ label: 'IDE', link: '/ide' },
						{ label: 'Monitoring the generated schema', link: '/schema-dump' },
					],
				},
				{
					label: 'Integrations',
					items: [
						{ label: 'Ktor', link: '/ktor' },
						{ label: 'http4k', link: '/http4k' },
						{ label: 'Spring', link: '/spring' },
					],
				},
				{
					label: 'Apollo Federation',
					items: [{ label: 'Apollo Federation', link: '/federation' }],
				},
				{
					label: 'Apollo Usage Reporting',
					items: [{ label: 'Apollo Usage Reporting', link: '/usage-reporting' }],
				},
				{ label: 'KDoc↗', link: 'https://martinbonnin.github.io/galak/kdoc/index.html' },
				{ label: 'GitHub↗', link: 'https://github.com/martinbonnin/galak' },
			],
		}),
	],
});
