// GenerateContent's output limit includes reasoning tokens. Use low effort for
// these short mobile responses; 2.5 and image models have different controls.
export function mobileGenerationConfig(model, config) {
  const modernText = /^gemini-3(?:\.\d+)?-(?:flash(?:-lite)?|pro)(?:-preview)?$/.test(model) || model === 'gemini-4-argon';
  return modernText ? { ...config, thinkingConfig: { thinkingLevel: 'low' } } : config;
}
